package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.repo.api.WeekViewRepo
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.editor.TodoItemEditorScreen
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.MenuWindow.Companion.MenuWindowKnobListener
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.WeekScreen.WeekScreenParameters
import ca.stefanm.ibus.gui.pim.calendar.views.editor.CalendarEventEditScreen
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.TimeSlotBars
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi



@ScreenDoc(
    screenName = "WeekScreen",
    description = "Week/agenda view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class WeekScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val calendarOptionsMenu: CalendarOptionsMenu,
    private val weekViewRepo: WeekViewRepo,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "WeekScreen"

        fun openWithParameters(
            navigationNodeTraverser: NavigationNodeTraverser,
            parameters: WeekScreenParameters
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                WeekScreen::class.java,
                parameters
            )
        }
    }

    data class WeekScreenParameters(
        val numberOfDays: Int
    )

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = WeekScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        var numberOfDays : Int = 3
        if (params != null && params.requestParameters is WeekScreenParameters) {
            numberOfDays = params.requestParameters.numberOfDays
        }


        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        val knobListenerService = MenuWindowKnobListener.current
        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            knobState.subscribeEvents()
        }


        Column(Modifier
            .fillMaxSize()
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            AgendaScreen(
                knobListenerService = knobListenerService,
                logger = logger,
                numberOfDays = numberOfDays,
                eventData = weekViewRepo.getAgendaViewEvents(
                    today,
                    numberOfDays
                ).map { list ->
                    list.map { event ->
                        event.copy(
                            onClick = {
                                CalendarEventEditScreen
                                    .openEventForEditing(navigationNodeTraverser, event)
                            }
                        )
                    }
                }.collectAsState(emptyList()).value,
                onMenuClicked = {
                    showMenu()
                }
            )
        }
    }

    private fun showMenu() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Week View",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("New Event..", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        CalendarEventEditScreen.createNewEvent(navigationNodeTraverser)
                    }),
                    TextMenuItem("New Todo..", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        navigationNodeTraverser.navigateToNode(TodoItemEditorScreen::class.java)
                    }),
                    TextMenuItem("Calendar Main Screen...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        navigationNodeTraverser.goBack()
                    }),
                    TextMenuItem("Close Menu", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    })
                )
            )
        }

    }

}