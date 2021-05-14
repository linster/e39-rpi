package ca.stefanm.ibus.gui.di

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Named

@Module
class GuiModule {

    companion object {
        const val NOTIFICATION_HUB_WRITER = "notification_hub_writer"
        const val NOTIFICATION_HUB_COLLECTOR = "notification_hub_collector"
    }

    @Provides
    @Named(NOTIFICATION_HUB_WRITER)
    @ApplicationScope
    fun provideNotificationHubWriterStateFlow() : MutableSharedFlow<Notification> = MutableSharedFlow()

    @Provides
    @Named(NOTIFICATION_HUB_COLLECTOR)
    @ApplicationScope
    @JvmSuppressWildcards(suppress = false) //Magic.
    fun provideNotificationHubStateFlow(
        @Named(NOTIFICATION_HUB_WRITER) hotFlow : MutableSharedFlow<Notification>
    ) : SharedFlow<Notification> {
        return hotFlow.asSharedFlow()
    }
}