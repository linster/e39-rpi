package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarModuleScope
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class ConfigurablePlatform @Inject constructor(
    private val configuredCarComponentProvider: Provider<ConfiguredCarComponent.Builder>
) {

    var configuredCarComponent : ConfiguredCarComponent? = null
        private set

    var currentConfiguration : DeviceConfiguration? = null
        private set

    fun run(initialConfiguration: DeviceConfiguration = LaptopDeviceConfiguration()) {
        onNewDeviceConfiguration(currentConfiguration ?: initialConfiguration)
    }

    fun stop() {
        if (configuredCarComponent != null) {
            configuredCarComponent?.configurablePlatformServiceRunner()?.stop()
        }
    }


    fun onNewDeviceConfiguration(configuration: DeviceConfiguration) {
        //destroy and recreate the Platform.
        stop()
        configuredCarComponentProvider
            .get()
            .configuredCarModule(ConfiguredCarModule(configuration))
            .build()
            .configurablePlatformServiceRunner()
            .run()
        currentConfiguration = configuration
    }
}

@ConfiguredCarModuleScope
class ConfigurablePlatformServiceRunner @Inject constructor(
    private val list: PlatformServiceList
) {
    fun run() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onCreate()
            }
        }
    }

    fun stop() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onShutdown()
            }
        }
    }
}