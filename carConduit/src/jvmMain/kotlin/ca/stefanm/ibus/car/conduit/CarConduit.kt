package ca.stefanm.ibus.car.conduit

import ca.stefanm.ibus.car.platform.CarPlatformConfiguration
import ca.stefanm.ibus.car.di.UnConfiguredCarComponent
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.car.platform.PlatformServiceGroup
import ca.stefanm.ibus.car.data.IBusMessage
import ca.stefanm.ibus.car.data.InputEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

actual class CarConduit @Inject constructor(){

    private companion object {
        val unConfiguredCarComponent : UnConfiguredCarComponent? = null

        val foo = Dagge
    }

    actual fun inputEventsFromCar() = carConduitComponentAccessor.inputEvents
    actual fun ibusMessagesFromCar(): SharedFlow<IBusMessage> {
        return carConduitComponentAccessor.incomingMessages.asSharedFlow()
    }

    actual fun saveNewCarConfiguration(carPlatformConfiguration: CarPlatformConfiguration) {
    }

    actual fun startPlatform() {
    }

    actual fun stopPlatform() {
    }

    actual fun setAutostartPlatform(autoStartOnBoot: Boolean) {
    }

    actual fun getAvailablePlatformServices(): List<PlatformServiceGroup> {
        TODO("Not yet implemented")
    }

    actual fun setDefaultRunStatusForPlatformService(
        service: PlatformService,
        startOnBoot: Boolean
    ) {
    }


}