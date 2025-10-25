package ca.stefanm.ca.stefanm.ibus.gui.calendar.views

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject


enum class CalendarView {
    MonthCalendar,
    WeekCalendar,
    TodaysAgenda,
    TodoList
}

class CalendarOptionsMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) {

    fun showCalendarOptionsMenu(
        onViewModeSelected : (new : CalendarView) -> Unit = {}
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
                        showViewModeSelect {  }
                    }),
                    TextMenuItem("New Event", onClicked = {  }),
                    TextMenuItem("New Todo", onClicked = {  }),
                    TextMenuItem("Go Back", onClicked = {
                        navigationNodeTraverser.navigateToRoot()
                    })
                )
            )
        }
    }


    fun showViewModeSelect(
        onViewModeSelected : (new : CalendarView) -> Unit
    ) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Select View",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Month", onClicked = { onViewModeSelected(CalendarView.MonthCalendar) }),
                    TextMenuItem("Week", onClicked = { onViewModeSelected(CalendarView.WeekCalendar) }),
                    TextMenuItem("Agenda", onClicked = { onViewModeSelected(CalendarView.TodaysAgenda) }),
                    TextMenuItem("TodoList", onClicked = { onViewModeSelected(CalendarView.TodoList) }),
                    TextMenuItem("Go Back", onClicked = {})
                ).map { it.copy(onClicked = { it.onClicked; modalMenuService.closeSidePaneOverlay(true)}) }
            )
        }
    }

}