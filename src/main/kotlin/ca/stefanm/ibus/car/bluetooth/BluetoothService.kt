package ca.stefanm.ibus.car.bluetooth

import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bluetooth.blueZdbus.DbusConnector
import ca.stefanm.ibus.car.bluetooth.blueZdbus.DbusReconnector
import ca.stefanm.ibus.car.bluetooth.blueZdbus.DbusTrackListenerService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.DBusMap
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.errors.UnknownObject
import org.freedesktop.dbus.interfaces.Properties
import java.lang.NullPointerException
import java.rmi.activation.UnknownObjectException
import javax.inject.Inject
import javax.inject.Named


//https://github.com/aguedes/bluez/blob/master/doc/media-api.txt

@ConfiguredCarScope
@PlatformServiceInfo(
    name = "BluetoothService",
    description = "Monolithic bluetooth service. TODO Should be split up."
)
@BluetoothServiceGroup
class BluetoothService @Inject constructor(
    private val onScreenSetupManager: BluetoothOnScreenSetupManager,
    private val bluetoothEventDispatcherService: BluetoothEventDispatcherService,
    private val trackInfoPrinter: TrackInfoPrinter,
    private val dBusTrackInfoFetcher: DBusTrackInfoFetcher,
    private val dbusTrackListenerService: DbusTrackListenerService,
    private val dbusConnector: DbusConnector,
    private val dbusReconnector: DbusReconnector,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override fun onCreate() {
        dbusConnector.onCreate()
        trackInfoPrinter.onCreate()
        dBusTrackInfoFetcher.onCreate()
        dbusTrackListenerService.onCreate()
        bluetoothEventDispatcherService.onCreate()
        super.onCreate()
    }

    override fun onShutdown() {
        dbusConnector.onShutdown()
        dbusTrackListenerService.onShutdown()
        dBusTrackInfoFetcher.onShutdown()
        trackInfoPrinter.onShutdown()
        bluetoothEventDispatcherService.onShutdown()
        super.onShutdown()
    }

    override suspend fun doWork() {

        val pairedPhone = with(onScreenSetupManager) {
            if (!isPhonePaired()) {
                requestBluetoothSetup()
            }
            getPairedPhone()
        }

        val btPhone = dbusConnector.getDevice(pairedPhone.macAddress)
        logger.d("BT", "have phone.")

        val player = dbusConnector.getPlayer(btPhone)
        bluetoothEventDispatcherService.mediaPlayer1 = player

        dBusTrackInfoFetcher.dbusConnection = dbusConnector.connection
        dBusTrackInfoFetcher.player = player

        dbusReconnector.previouslyPairedPhone = pairedPhone

        dbusTrackListenerService.btPhone = btPhone
        dbusTrackListenerService.dbusConnection = dbusConnector.connection
        dbusTrackListenerService.mediaPlayer1 = player
    }


}

@ConfiguredCarScope
class DBusTrackInfoFetcher @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

    private val dbusReconnector: DbusReconnector,
    private val trackInfoPrinter: TrackInfoPrinter,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    private val TAG = "Track Fetcher"

    var dbusConnection : DBusConnection? = null
    var player : MediaPlayer1? = null

    override suspend fun doWork() {
        inputEvents.collect {
            if (it == InputEvent.PrevTrack || it == InputEvent.NextTrack) {
                val (track, artist, album) = fetchNewTrackInfo()
                trackInfoPrinter.onNewTrackInfo(track, artist, album)
            }
        }
    }

    private suspend fun fetchNewTrackInfo() : Triple<String, String, String> {

        //Wait here so that we can actually switch to the next track.
        //TODO this a race condition with the piece that calls MediaPlayer1 below.
        delay(1000)

        val rawMap = if (dbusConnection != null && player != null) {
            try {
                getRawTrackInfo(dbusConnection!!, player!!)
            } catch (e : Throwable) {
                if (e is NullPointerException) {
                    logger.e(TAG, "Dbus connection and player changed on us to null?!", e)
                }

                val (newConnection, newPlayer) = dbusReconnector.reconnect()
                dbusConnection = newConnection
                player = newPlayer

                if (e is UnknownObjectException) {
                    logger.w(TAG,"We tried reconnecting but we're too quick. ")
                    delay(500)
                }

                mapOf<String, String>()
            }
        } else {
            logger.d(TAG, "dbus connection and player not set yet")

            val (newConnection, newPlayer) = dbusReconnector.reconnect()
            dbusConnection = newConnection
            player = newPlayer

            mapOf()
        }

        val track = rawMap.getOrElse("Title") { logger.d(TAG, "No track name") ; ""}
        val artist = rawMap.getOrElse("Artist"){ logger.d(TAG, "No artist name") ; ""}
        val album = rawMap.getOrElse("Album"){ logger.d(TAG, "No album name") ; ""}

        return Triple(track, artist, album)
    }

    private fun getRawTrackInfo(dBusConnection: DBusConnection, mediaPlayer: MediaPlayer1) : Map<String, String>{
        return dBusConnection
            .getRemoteObject("org.bluez", mediaPlayer.objectPath, Properties::class.java)
            .Get<DBusMap<String, String>>("org.bluez.MediaPlayer1", "Track")
    }
}

@ConfiguredCarScope
class BluetoothEventDispatcherService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

    private val dbusReconnector: DbusReconnector,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    var mediaPlayer1: MediaPlayer1? = null

    override suspend fun doWork() {
        inputEvents.collect {
            dispatchInputEvent(it)
        }
    }

    private fun dispatchInputEvent(event: InputEvent) {
        if (mediaPlayer1 == null) {
            logger.w("BT Dispatcher", "Attempting to dispatch to MediaPlayer where it doesn't exist.")
            mediaPlayer1 = dbusReconnector.reconnect().second
        }
        try {
            when (event) {
                InputEvent.PrevTrack -> mediaPlayer1?.Previous()
                InputEvent.NextTrack -> mediaPlayer1?.Next()
            }
        } catch (e : UnknownObject) {
            logger.e("BT Dispatcher", "Unknown object?", e)
        }

    }
}