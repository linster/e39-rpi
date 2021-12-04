package ca.stefanm.ibus.car.di

import ca.stefanm.ibus.car.bluetooth.blueZdbus.CliTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.car.data.IBusMessage
import ca.stefanm.ibus.car.hardware.CliRelayReaderWriter
import ca.stefanm.ibus.car.hardware.RelayReaderWriter
import ca.stefanm.ibus.car.hardware.RpiRelayReaderWriter
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.car.serial.JSerialCommsAdapter
import ca.stefanm.ibus.car.serial.SerialPortReader
import ca.stefanm.ibus.car.serial.SerialPortWriter
import dagger.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.awt.event.InputEvent
import javax.inject.Named
import javax.inject.Scope





@Component(modules = [

])
interface UnConfiguredCarComponent {
    fun configuredCarComponent(configuredCarModule: ConfiguredCarModule) : ConfiguredCarComponent
}

@Module
class UnConfiguredCarModule {

    companion object {
        //this is for the cli platform.
        const val INITIAL_CONFIGURATION = "initial_configuration"
        const val INPUT_EVENTS = "input_events"
        const val INPUT_EVENTS_WRITER = "input_events_writer"
        //Messages sent to rest of car
        const val IBUS_MESSAGE_INGRESS = "IbusInput"
        const val IBUS_MESSAGE_OUTPUT_CHANNEL = "IbusOutput"
    }

    @Provides
    @Named(INPUT_EVENTS_WRITER)
    @UnConfiguredCarScope
    fun provideInputEventWriteStateFlow() : MutableSharedFlow<InputEvent> = MutableSharedFlow()

    @Provides
    @Named(INPUT_EVENTS)
    @UnConfiguredCarScope
    @JvmSuppressWildcards(suppress = false) //Magic.
    fun provideInputEventsStateFlow(
        @Named(INPUT_EVENTS_WRITER) hotFlow : MutableSharedFlow<InputEvent>
    ) : SharedFlow<InputEvent> {
        return hotFlow.asSharedFlow()
    }

    @Provides
    @Named(IBUS_MESSAGE_INGRESS)
    @UnConfiguredCarScope
    fun provideIbusIngressChannel() : MutableSharedFlow<IBusMessage> = MutableSharedFlow<IBusMessage>()

    @Provides
    @Named(IBUS_MESSAGE_OUTPUT_CHANNEL)
    @UnConfiguredCarScope
    fun provideIbusOuptutChannel() : Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)

}

