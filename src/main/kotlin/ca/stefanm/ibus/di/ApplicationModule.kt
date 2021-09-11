package ca.stefanm.ibus.di

import ca.stefanm.ibus.CliMain
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.logging.StdOutLogger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.gui.GuiMain
import ca.stefanm.ibus.gui.di.GuiModule
import ca.stefanm.ibus.gui.di.MapModule
import ca.stefanm.ibus.gui.map.widget.tile.TileFetcher
import ca.stefanm.ibus.gui.map.widget.tile.TileServerImageCacheClearer
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*
import javax.inject.Named
import javax.inject.Scope

@Scope
annotation class ApplicationScope

@Component(modules = [
    ApplicationModule::class,
    GuiModule::class,
    NavigationModule::class,
//    NavigationBindsModule::class,
    MapModule::class
])
@ApplicationScope
interface ApplicationComponent {

    fun configuredCarComponent(configuredCarModule: ConfiguredCarModule) : ConfiguredCarComponent

    fun logger() : Logger

    fun tileFetcher() : TileFetcher
    fun tileCacheClearer() : TileServerImageCacheClearer


    fun knobListenerService() : KnobListenerService
    fun modalMenuService() : ModalMenuService
//    @Named(ApplicationModule.INPUT_EVENTS) fun inputEvents() : SharedFlow<InputEvent>


    fun inject(cliMain : CliMain)
    fun inject(guiMain: GuiMain)
}

@ExperimentalCoroutinesApi
@Module
class ApplicationModule {


    @Named(INITIAL_CONFIGURATION)
    @Provides
    @ApplicationScope
    fun provideInitialConfiguration() : CarPlatformConfiguration = LaptopDeviceConfiguration()


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
    @ApplicationScope
    fun provideInputEventWriteStateFlow() : MutableSharedFlow<InputEvent> = MutableSharedFlow()

    @Provides
    @Named(INPUT_EVENTS)
    @ApplicationScope
    @JvmSuppressWildcards(suppress = false) //Magic.
    fun provideInputEventsStateFlow(
        @Named(INPUT_EVENTS_WRITER) hotFlow : MutableSharedFlow<InputEvent>
    ) : SharedFlow<InputEvent> {
        return hotFlow.asSharedFlow()
    }

    @Provides
    @Named(IBUS_MESSAGE_INGRESS)
    @ApplicationScope
    fun provideIbusIngressChannel() : MutableSharedFlow<IBusMessage> = MutableSharedFlow<IBusMessage>()

    @Provides
    @Named(IBUS_MESSAGE_OUTPUT_CHANNEL)
    @ApplicationScope
    fun provideIbusOuptutChannel() : Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)



    //TODO
    //TODO https://arunkumar.dev/introducing-scabbard-a-tool-to-visualize-dagger-2-dependency-graphs/


    @Provides
    @ApplicationScope
    fun provideCoroutineScope() : CoroutineScope = GlobalScope

    @Provides
    @ApplicationScope
    fun provideCoroutineDispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Provides
    @ApplicationScope
    fun provideLogger() : Logger {
        return StdOutLogger()
    }
}