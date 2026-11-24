package ca.stefanm.ibus.lib.hardwareDrivers.gps

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.GpsLocationProvidersServiceGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Reads the car's position from gpsd (JSON over TCP, localhost:2947 on the
 * Pi's image, where gpsd is socket-activated: connecting starts it), and
 * reports it to the [LocationRepository].
 *
 * Connects, sends ?WATCH, and from gpsd's TPV, SKY, DEVICES and DEVICE
 * messages reports the fix, the state (no gpsd / no receiver / searching /
 * fix / stale) and the satellites. When gpsd isn't there or the connection
 * drops, it tries again with backoff ([MIN_BACKOFF] doubling to
 * [MAX_BACKOFF]). A fix is stale after `GpsConfig.staleAfterSeconds` without a
 * new one, or as soon as gpsd reports no fix.
 */
@PlatformServiceInfo(
    name = GpsdService.NAME,
    description = "Reads the car's position from gpsd (E39Config.GpsConfig: localhost:2947 by default) " +
            "into the LocationRepository, reconnecting when gpsd isn't there."
)
@GpsLocationProvidersServiceGroup
@ConfiguredCarScope
class GpsdService internal constructor(
    private val host: String,
    private val port: Int,
    private val staleAfter: Duration,
    private val locationRepository: LocationRepository,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    private val minBackoff: Duration = MIN_BACKOFF,
    private val maxBackoff: Duration = MAX_BACKOFF,
    private val stateCheckInterval: Duration = STATE_CHECK_INTERVAL,
    private val nowMs: () -> Long = { System.nanoTime() / 1_000_000 },
) : LongRunningService(coroutineScope, dispatcher) {

    @Inject constructor(
        configurationStorage: ConfigurationStorage,
        locationRepository: LocationRepository,
        logger: Logger,
        @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
        @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) dispatcher: CoroutineDispatcher,
    ) : this(
        host = configurationStorage.config[E39Config.GpsConfig.gpsdHost],
        port = configurationStorage.config[E39Config.GpsConfig.gpsdPort],
        staleAfter = configurationStorage.config[E39Config.GpsConfig.staleAfterSeconds].seconds,
        locationRepository = locationRepository,
        logger = logger,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher,
    )

    companion object {
        const val TAG = "GpsdService"
        /** Its name in the car platform's service list, and as a [LocationRepository] source. */
        const val NAME = "GpsdService"
        val MIN_BACKOFF = 1.seconds
        val MAX_BACKOFF = 30.seconds
        val CONNECT_TIMEOUT = 2.seconds
        /** How often the state is checked for a fix going stale. */
        val STATE_CHECK_INTERVAL = 500.milliseconds
    }

    private val lock = Any()
    /** Between doWork() and onShutdown(): a stopped service reports nothing more. */
    private var running = false
    private var connected = false
    /** gpsd's receivers (device paths); empty when not connected. */
    private var devices: Set<String> = emptySet()
    /** Whether this service has reported a fix (the repository keeps it as the last known position). */
    private var hadFix = false
    /** When the last fix arrived ([nowMs]); null when there isn't a current one (gpsd said no fix). */
    private var lastFixAtMs: Long? = null
    /** The state last reported. */
    private var reportedState: GpsState? = null

    override suspend fun doWork() {
        coroutineScope {
            synchronized(lock) { running = true }
            updateState()
            // Notices fixes going stale between messages (gpsd sends nothing without a receiver).
            launch {
                while (isActive) {
                    delay(stateCheckInterval)
                    updateState()
                }
            }

            var backoff = minBackoff
            var failures = 0
            while (isActive) {
                val gotData = try {
                    session()
                } catch (e: IOException) {
                    currentCoroutineContext().ensureActive()
                    // Logged once per outage, not on every retry.
                    if (failures == 0) logger.w(TAG, "gpsd at $host:$port: ${e.message}; retrying")
                    false
                }
                failures = if (gotData) 0 else failures + 1
                if (gotData) backoff = minBackoff
                delay(backoff)
                backoff = minOf(backoff * 2, maxBackoff)
            }
        }
    }

    /** One connection to gpsd, until it closes. Whether gpsd sent anything. */
    private suspend fun session(): Boolean = coroutineScope {
        val socket = Socket()
        // A blocking read doesn't see cancellation: closing the socket ends it.
        val closer = launch {
            try { awaitCancellation() } finally { socket.close() }
        }
        var gotData = false
        try {
            socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT.inWholeMilliseconds.toInt())
            socket.tcpNoDelay = true
            synchronized(lock) { connected = true }
            logger.i(TAG, "Connected to gpsd at $host:$port")
            updateState()

            socket.getOutputStream().apply {
                write(GpsdMessage.WATCH.toByteArray(Charsets.US_ASCII))
                flush()
            }
            val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
            while (true) {
                val line = reader.readLine() ?: break
                gotData = true
                GpsdMessage.parse(line)?.let { handle(it) }
            }
            logger.w(TAG, "gpsd closed the connection")
            gotData
        } catch (e: IOException) {
            currentCoroutineContext().ensureActive()
            if (gotData) {
                logger.w(TAG, "Lost gpsd: ${e.message}")
                gotData
            } else {
                throw e
            }
        } finally {
            closer.cancel()
            socket.close()
            synchronized(lock) {
                connected = false
                lastFixAtMs = null
                devices = emptySet()
            }
            if (synchronized(lock) { running }) locationRepository.reportSatellites(NAME, null)
            updateState()
        }
    }

    private fun handle(message: GpsdMessage) {
        when (message) {
            is GpsdMessage.Version -> logger.d(TAG, "gpsd ${message.release}")
            is GpsdMessage.Devices -> synchronized(lock) { devices = message.paths.toSet() }
            is GpsdMessage.Device -> synchronized(lock) {
                devices = if (message.active) {
                    devices + message.path
                } else {
                    logger.i(TAG, "Receiver ${message.path} is gone")
                    devices - message.path
                }
            }
            is GpsdMessage.Tpv -> {
                val fix = message.toFix()
                synchronized(lock) {
                    // A TPV comes from a receiver, even if DEVICES didn't list it yet.
                    message.device?.let { devices = devices + it }
                    if (fix != null) {
                        hadFix = true
                        lastFixAtMs = nowMs()
                    } else if (message.mode == 1) {
                        // gpsd says there's no fix: the last one isn't current any more.
                        lastFixAtMs = null
                    }
                }
                if (fix != null && synchronized(lock) { running }) locationRepository.reportFix(NAME, fix)
            }
            is GpsdMessage.Sky -> if (synchronized(lock) { running }) {
                locationRepository.reportSatellites(NAME, message.satellites)
            }
            is GpsdMessage.Other -> {}
        }
        updateState()
    }

    private fun updateState() {
        val new = synchronized(lock) {
            if (!running) return
            val fixAt = lastFixAtMs
            val state = when {
                !connected -> GpsState.NO_GPSD
                devices.isEmpty() -> GpsState.NO_RECEIVER
                fixAt != null && nowMs() - fixAt <= staleAfter.inWholeMilliseconds -> GpsState.FIX
                hadFix -> GpsState.STALE
                else -> GpsState.SEARCHING
            }
            if (state == reportedState) return
            reportedState = state
            state
        }
        locationRepository.reportState(NAME, new)
    }

    override fun onShutdown() {
        super.onShutdown()
        synchronized(lock) {
            running = false
            connected = false
            lastFixAtMs = null
            devices = emptySet()
            reportedState = null
        }
        locationRepository.sourceStopped(NAME)
    }
}
