package ca.stefanm.ibus.di

import ca.stefanm.ibus.car.bluetooth.blueZdbus.CliTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModuleScope
import ca.stefanm.ibus.lib.hardwareDrivers.CliRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RpiRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.JSerialCommsAdapter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortReader
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortWriter
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.logging.StdOutLogger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Named
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module(subcomponents = [ConfiguredCarComponent::class])
class ApplicationModule {



    @Named(INITIAL_CONFIGURATION)
    @Provides
    fun provideInitialConfiguration() : DeviceConfiguration = LaptopDeviceConfiguration()


    companion object {
        const val INITIAL_CONFIGURATION = "initial_configuration"
        const val INPUT_EVENTS = "input_events"
        const val INPUT_EVENTS_WRITER = "input_events_writer"
        //Messages sent to rest of car
        const val IBUS_MESSAGE_INGRESS = "IbusInput"
        const val IBUS_MESSAGE_OUTPUT_CHANNEL = "IbusOutput"
    }

    @Provides
    @Named(INPUT_EVENTS_WRITER)
    fun provideInputEventWriteStateFlow() : MutableSharedFlow<InputEvent> = MutableSharedFlow()

    @Provides
    @Named(INPUT_EVENTS)
    @Singleton
    fun provideInputEventsStateFlow(
        @Named(INPUT_EVENTS_WRITER) hotFlow : MutableSharedFlow<InputEvent>
    ) : SharedFlow<InputEvent> {
        return hotFlow.asSharedFlow()
    }


    @Provides
    @Named(IBUS_MESSAGE_INGRESS)
    fun provideIbusIngressChannel() : MutableSharedFlow<IBusMessage> = MutableSharedFlow<IBusMessage>()

    @Provides
    @Named(IBUS_MESSAGE_OUTPUT_CHANNEL)
    fun provideIbusOuptutChannel() : Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)



    //TODO
    //TODO https://arunkumar.dev/introducing-scabbard-a-tool-to-visualize-dagger-2-dependency-graphs/


    @Provides
    @Singleton
    fun provideCoroutineScope() : CoroutineScope = GlobalScope

    @Provides
    @Singleton
    fun provideCoroutineDispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideLogger() : Logger {
        return StdOutLogger()
    }
}