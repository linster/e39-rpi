package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.CalendarScreen
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarView
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarViewConfigRepo
import javax.inject.Inject


class CalendarOptionsMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val calendarViewConfigRepo: CalendarViewConfigRepo,
    private val modalMenuService: ModalMenuService
) {

    fun showCalendarOptionsMenu(
    ) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Calendar Options",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Change View", onClicked = {
                        showViewModeSelect()
                    }),
                    TextMenuItem("New Event", onClicked = {  }),
                    TextMenuItem("New Todo", onClicked = {  }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        navigationNodeTraverser.navigateToRoot()
                    })
                )
            )
        }
    }

    private fun changeView(view : CalendarView) {
        modalMenuService.closeSidePaneOverlay(true)
        calendarViewConfigRepo.screenView.value = view
        navigationNodeTraverser.cleanupBackStackDescendentsOf(CalendarScreen::class.java)
        navigationNodeTraverser.navigateToNode(CalendarScreen::class.java)
    }


    fun showViewModeSelect() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Select View",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Month", onClicked = { changeView(CalendarView.MonthCalendar) }),
                    TextMenuItem("One Week", onClicked = { changeView(CalendarView.OneWeekCalendar) }),
                    TextMenuItem("Two Week", onClicked = { changeView(CalendarView.TwoWeekCalendar) }),
                    TextMenuItem("Agenda", onClicked = { changeView(CalendarView.TodaysAgenda) }),
                    TextMenuItem("TodoList", onClicked = { changeView(CalendarView.TodoList) })
                )
            )
        }
    }

}