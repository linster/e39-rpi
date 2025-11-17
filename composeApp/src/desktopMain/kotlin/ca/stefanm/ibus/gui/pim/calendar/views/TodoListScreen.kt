package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ScreenDoc(
    screenName = "TodoListScreen",
    description = "Todo list view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class TodoListScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val calendarOptionsMenu: CalendarOptionsMenu
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = TodoListScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit =  { params ->

    }
}