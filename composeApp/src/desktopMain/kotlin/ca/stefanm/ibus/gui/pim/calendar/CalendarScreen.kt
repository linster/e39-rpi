package ca.stefanm.ibus.gui.pim.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarView
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarViewConfigRepo
import ca.stefanm.ibus.gui.pim.calendar.views.*
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@ScreenDoc(
    screenName = "CalendarScreen",
    description = "The main menu for the matrix chat client settings",
    navigatesTo = [
        ScreenDoc.NavigateTo(
            MonthScreen::class,
        )
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class CalendarScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val calendarOptionsMenu: CalendarOptionsMenu,
    private val calendarViewConfigRepo: CalendarViewConfigRepo
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CalendarScreen::class.java


    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(modifier = Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Calendar")
            FullScreenMenu.OneColumn(items = listOf(
                TextMenuItem(
                    "Loading...",
                    isSelectable = false,
                    onClicked = {}
                ),
                TextMenuItem(
                    "Go back",
                    onClicked = {
                        navigationNodeTraverser.navigateToRoot()
                    }
                )
            ))

            //https://github.com/kizitonwose/Calendar/blob/main/docs/Compose.md


            LaunchedEffect(Unit) {
//                navigationNodeTraverser.navigateToNode(MonthScreen::class.java)
                delay(10)
                val view = calendarViewConfigRepo.screenView
                when (view.value) {
                    CalendarView.MonthCalendar -> navigationNodeTraverser.navigateToNode(MonthScreen::class.java)
                    CalendarView.OneWeekCalendar -> navigationNodeTraverser.navigateToNode(OneWeekScreen::class.java)
                    CalendarView.TwoWeekCalendar -> navigationNodeTraverser.navigateToNode(TwoWeekScreen::class.java)
                    CalendarView.TodaysAgenda,
                    CalendarView.TodoList -> navigationNodeTraverser.navigateToNode(TodoListScreen::class.java)
                }

            }

        }
    }
}