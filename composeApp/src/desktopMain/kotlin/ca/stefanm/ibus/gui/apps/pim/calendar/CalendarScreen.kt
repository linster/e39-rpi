package ca.stefanm.ibus.gui.pim.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ca.stefanm.ibus.gui.pim.calendar.setup.views.CalendarSetupRootScreen
import ca.stefanm.ibus.gui.pim.calendar.views.*
import ca.stefanm.ibus.gui.pim.calendar.views.editor.CalendarEventEditScreen
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
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

            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(TextMenuItem(
                    "Go back",
                    onClicked = {
                        navigationNodeTraverser.navigateToRoot()
                    }
                )),
                ne = listOf(
                    TextMenuItem(
                        "Month",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(MonthScreen::class.java)
                        }
                    ),
                    TextMenuItem(
                        "Week (1 day)",
                        onClicked = { WeekScreen.openWithParameters(navigationNodeTraverser, WeekScreen.WeekScreenParameters(numberOfDays = 1)) }
                    ),
                    TextMenuItem(
                        "Week (3 days)",
                        onClicked = { WeekScreen.openWithParameters(navigationNodeTraverser, WeekScreen.WeekScreenParameters(numberOfDays = 3))}
                    ),
                    TextMenuItem(
                        "Week (5 days)",
                        onClicked = { WeekScreen.openWithParameters(navigationNodeTraverser, WeekScreen.WeekScreenParameters(numberOfDays = 5))}
                    ),
                    TextMenuItem(
                        "Todo List",
                        onClicked = {}
                    )
                ),
                sw = listOf(
                    TextMenuItem(
                        "New Event",
                        onClicked = {
                            CalendarEventEditScreen.createNewEvent(navigationNodeTraverser)
                        }
                    ),
                    TextMenuItem(
                        "Settings",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(CalendarSetupRootScreen::class.java)
                        }
                    )
                ),
                se = listOf()
            )

        }
    }
}