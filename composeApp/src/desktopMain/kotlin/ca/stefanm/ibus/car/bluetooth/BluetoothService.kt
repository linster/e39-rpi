package ca.stefanm.ca.stefanm.ibus.car.bluetooth

import ca.stefanm.ca.stefanm.ibus.car.bluetooth.blueZdbus.FlowDbusConnector
import ca.stefanm.ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


//https://github.com/aguedes/bluez/blob/master/doc/media-api.txt

@ConfiguredCarScope
@PlatformServiceInfo(
    name = "BluetoothService",
    description = "Service that binds the pairedPhone from configuration to the flowDbusConnector, allowing all" +
            "other components to just work."
)
@BluetoothServiceGroup
class BluetoothService @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val pairedPhone: CarPlatformConfiguration.PairedPhone?,

    private val flowDbusConnector: FlowDbusConnector,

    private val logger: Logger,
    private val notificationHub: NotificationHub,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override fun onCreate() {
        super.onCreate()
        // Might have to have utility methods to restart the service when there's a new mac addess change?
        flowDbusConnector.macAddress.value = pairedPhone?.macAddress
    }


    override suspend fun doWork() {
        coroutineScope.launch {
            flowDbusConnector.getDevice().collect {
                logger.d("BT", "have phone. $it")
                notificationHub.postNotification(
                    Notification(
                        Notification.NotificationImage.BLUETOOTH,
                        "Have Phone",
                        "Phone: ${it.name}"
                    )
                )
            }
        }

        coroutineScope.launch {
            flowDbusConnector.getPlayer().collect {
                logger.d("BT", "have MediaPlayer. $it")
                notificationHub.postNotification(
                    Notification(
                        Notification.NotificationImage.BLUETOOTH,
                        "Have MediaPlayer",
                        "MediaPlayer: ${it?.objectPath}"
                    )
                )
            }
        }
    }


}

