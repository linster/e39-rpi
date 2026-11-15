package ca.stefanm.ibus.lib.audio

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "BluetoothAudioRoleService",
    description = "At start-up, tells WirePlumber the Bluetooth audio role from the config " +
            "(receive from phones, or play to a speaker; Settings -> Audio)."
)
@BluetoothServiceGroup
@ConfiguredCarScope
class BluetoothAudioRoleService @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val applier: BluetoothAudioRoleApplier,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) dispatcher: CoroutineDispatcher,
) : LongRunningService(coroutineScope, dispatcher) {

    override suspend fun doWork() {
        if (!applier.canChange()) return
        val role = BluetoothAudioRole.fromConfig(configurationStorage.config[E39Config.AudioConfig.bluetoothRole])
        // Restarts WirePlumber only if its fragment says something else.
        applier.apply(role).onSuccess { changed ->
            logger.i(BluetoothAudioRoleApplier.TAG, "Bluetooth audio role ${role.configValue}" +
                    if (changed) " applied" else " (already set)")
        }
    }
}
