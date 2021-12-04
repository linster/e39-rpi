package ca.stefanm.ibus.car.conduit

import ca.stefanm.ibus.car.data.InputEvent
import ca.stefanm.ibus.car.platform.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.car.platform.PlatformServiceGroup
import ca.stefanm.ibus.car.data.IBusMessage
import kotlinx.coroutines.flow.SharedFlow


expect class CarConduit {

    fun inputEventsFromCar() : SharedFlow<InputEvent>
    fun ibusMessagesFromCar() : SharedFlow<IBusMessage>

    fun saveNewCarConfiguration(carPlatformConfiguration: CarPlatformConfiguration)
    fun startPlatform()
    fun stopPlatform()
    fun setAutostartPlatform(autoStartOnBoot : Boolean)


    fun getAvailablePlatformServices() : List<PlatformServiceGroup>


    fun setDefaultRunStatusForPlatformService(service: PlatformService, startOnBoot : Boolean)

}