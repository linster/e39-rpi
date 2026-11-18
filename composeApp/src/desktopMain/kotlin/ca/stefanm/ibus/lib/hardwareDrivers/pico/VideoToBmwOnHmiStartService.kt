package ca.stefanm.ibus.lib.hardwareDrivers.pico

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.SerialInterfaceServiceGroup
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named

/**
 * Once the HMI has started, asks the Pico to switch the screen's video input to the BMW picture
 * (upstream), as the "Back to BMW" menu item does (linster/e39-rpi#56).
 *
 * "The HMI has started" is the first platform start in this process: at startup the loading window
 * opens the HMI main window (showing the main menu) and then starts the platform. An HMI restart (a
 * crash, the Pico's restart request, Settings) restarts the process through systemd, so it switches
 * again on every HMI start. Restarting the platform without restarting the process (e.g. applying a
 * new platform configuration) doesn't switch again: that would take the user off the Pi's screen
 * while they're using it.
 *
 * Only where there's a Pico (CarPlatformConfiguration.hasPico: an E39 with IBusConnection = PICO,
 * also on a laptop wired to one), and only while the SwitchVideoToBmwOnHmiStart setting is on (the
 * default). The packet is queued on the IBus output
 * channel, which SerialPublisherService (same service group) sends.
 */
@PlatformServiceInfo(
    name = "VideoToBmwOnHmiStart",
    description = "Once the HMI has started, asks the Pico to show the BMW picture (setting SwitchVideoToBmwOnHmiStart)."
)
@SerialInterfaceServiceGroup
class VideoToBmwOnHmiStartService @Inject constructor(
    private val logger: Logger,
    private val configurationStorage: ConfigurationStorage,
    private val deviceConfiguration: CarPlatformConfiguration,
    private val picoScreenStatusManager: PicoScreenStatusManager,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) dispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, dispatcher) {

    private companion object {
        const val TAG = "VideoToBmwOnHmiStart"
        /** Once per process (HMI start), not once per platform start. */
        val sentThisProcess = AtomicBoolean(false)
    }

    override suspend fun doWork() {
        if (!configurationStorage.config[E39Config.CarPlatformConfigSpec._switchVideoToBmwOnHmiStart]) {
            logger.d(TAG, "Not switching the video input to BMW: SwitchVideoToBmwOnHmiStart is off")
            return
        }
        if (!deviceConfiguration.hasPico) {
            logger.d(TAG, "Not switching the video input to BMW: no Pico on this platform " +
                    "(MachineType=${deviceConfiguration.machineType}, IBusConnection=${deviceConfiguration.iBusConnection})")
            return
        }
        if (!sentThisProcess.compareAndSet(false, true)) {
            return // the platform was restarted, not the HMI
        }
        picoScreenStatusManager.goBackToBmw()
        logger.i(TAG, "Video input: BMW (after HMI start)")
    }
}
