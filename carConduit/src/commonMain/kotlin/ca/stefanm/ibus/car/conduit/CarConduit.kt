package ca.stefanm.ibus.car.conduit

import ca.stefanm.ibus.car.conduit.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.logger.LogDistributionHub
import kotlinx.coroutines.flow.SharedFlow


interface CarConduit {

    fun inputEventsFromCar() : SharedFlow<InputEvent>
    fun ibusMessagesFromCar() : SharedFlow<IBusMessage>

    fun relayReaderWriter() : RelayReaderWriter

    fun saveNewCarConfiguration(carPlatformConfiguration: CarPlatformConfiguration)
    fun startPlatform()
    fun stopPlatform()
    fun setAutostartPlatform(autoStartOnBoot : Boolean)


    fun getAvailablePlatformServices()

}