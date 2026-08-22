package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.FileManagerViewState
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.ToolbarViews
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "FileManagerScreen",
    description = "The main screen for the file manager app."
)
@AutoDiscover
class FileManagerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX)
    private val knobListenerServiceMainAux: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "FileManagerScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FileManagerScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

        val knobStateMain = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerServiceMain,
            logger,
            TAG
        )

        val viewState = FileManagerViewState()

        ToolbarViews.Toolbar(
            knobState = knobStateMain,
            modalMenuService = modalMenuService,
            viewState = viewState,
            onViewStateChanged = {}
        )
    }
}