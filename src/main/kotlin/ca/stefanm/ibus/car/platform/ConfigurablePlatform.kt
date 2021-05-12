package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@ApplicationScope
class ConfigurablePlatform @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>
) {

    var configurablePlatformServiceRunner: ConfigurablePlatformServiceRunner? = null
    var configuredCarComponent : ConfiguredCarComponent? = null

    var currentConfiguration : DeviceConfiguration? = null
        private set

    fun run(initialConfiguration: DeviceConfiguration = LaptopDeviceConfiguration()) {
        onNewDeviceConfiguration(currentConfiguration ?: initialConfiguration)
    }

    fun stop() {
        configurablePlatformServiceRunner?.stop()
        configurablePlatformServiceRunner = null
    }


    fun onNewDeviceConfiguration(configuration: DeviceConfiguration) {
        //destroy and recreate the Platform.
        stop()

        configuredCarComponent = DaggerApplicationComponent.create()
            .configuredCarComponent(ConfiguredCarModule(configuration))

        configurablePlatformServiceRunner =
            configuredCarComponent?.configurablePlatformServiceRunner()

        configurablePlatformServiceRunner?.run()
        currentConfiguration = configuration
    }
}

@ConfiguredCarScope
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