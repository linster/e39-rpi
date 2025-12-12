package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.repo.api.WeekViewRepo
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.NorthButtonRow
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.TimeSlotBars
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ScreenDoc(
    screenName = "OneWeekScreen",
    description = "One-week calendar view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class OneWeekScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val calendarOptionsMenu: CalendarOptionsMenu,
    private val weekViewRepo: WeekViewRepo,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "OneWeekScreen"
    }
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = OneWeekScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        OneWeekCalendar()
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    fun OneWeekCalendar() {

        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            knobState.subscribeEvents()
        }


        Column(Modifier.fillMaxSize()) {


            NorthButtonRow(
                knobState = knobState,
                timePeriodLabel = "Some Week",
                onPreviousClicked = {
                    scope.launch {
                        logger.d(TAG, "Prev week clicked")
                    }
                },
                onNextClicked = {
                    logger.d(TAG, "Get clicked into next week")
                },
                onMenuClicked = {
                    calendarOptionsMenu.showCalendarOptionsMenu()
                }
            )

            Box(
                modifier = Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            ) {
                TimeSlotBars()
            }
        }
    }
}