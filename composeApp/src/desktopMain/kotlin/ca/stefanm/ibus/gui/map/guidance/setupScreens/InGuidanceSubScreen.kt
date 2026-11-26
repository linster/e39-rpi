package ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import ca.stefanm.ibus.gui.docs.GuidanceScreenDocPartition
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSetupScreenInstructionConsumer
import ca.stefanm.ibus.gui.map.guidance.SideBarGuidanceInstructionConsumer
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.map.guidance.GuidanceIcons
import ca.stefanm.ibus.gui.map.guidance.GuidanceInstruction
import ca.stefanm.ibus.gui.map.guidance.notifications.ui.GuidanceSidePanel
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.toView
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import com.javadocmd.simplelatlng.LatLngTool
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScreenDoc(
    screenName = "InGuidanceSubScreen",
    description = "When a guidance session is in progress, this is the screen that a map " +
            "`Route Guidance` action goes to to allow the user to modify or end the guidance" +
            " session.",
    navigatesTo = [
        ScreenDoc.NavigateTo(MapScreen::class, linkDescription = "Back to Map")
    ]
)

@ScreenDoc.AllowsGoBack
@GuidanceScreenDocPartition
@AutoDiscover
class InGuidanceSubScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val guidanceService : GuidanceService,
    private val guidanceInstructionConsumer: GuidanceSetupScreenInstructionConsumer,
    private val sideBarGuidanceInstructionConsumer: SideBarGuidanceInstructionConsumer,
    private val notificationHub: NotificationHub,
    private val gpsMapCenterProvider: GpsMapCenterProvider,
    private val locationRepository: LocationRepository
) : NavigationNode<GuidanceSetupScreen.SubScreenResult> {

    companion object {
        const val TAG = "InGuidanceSubScreen"
    }

    override val thisClass: Class<out NavigationNode<GuidanceSetupScreen.SubScreenResult>> = InGuidanceSubScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val currentSession = guidanceService.getGuidanceSessionState().collectAsState(null)

        when (currentSession.value) {
            null -> { /* Do nothing, flow hasn't read from disk yet. */ }
            GuidanceSession.SessionState.IN_GUIDANCE -> {
                Contents()
            }
            GuidanceSession.SessionState.SETTING_UP,
            GuidanceSession.SessionState.READY_TO_CALCULATE,
            GuidanceSession.SessionState.ROUTE_CALCULATED,
            GuidanceSession.SessionState.TERMINATED -> {
                navigationNodeTraverser.setResultAndGoBack(
                    this, GuidanceSetupScreen.SubScreenResult.STATE_CHANGED
                )
            }
        }

    }

    @Composable
    fun Contents() {

        val instructions by guidanceService.instructions.collectAsState()
        val tracker by guidanceService.trackerState.collectAsState()
        //TODO need to setup the list of instructions and whether they are completed

        val toShowInstructions = remember(instructions, tracker) {
            derivedStateOf {
                instructions.map {
                    it to (it.index < (tracker.shownIndex ?: 0))
                }
            }
        }

        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("In Guidance")

            // A preview is only for this screen: leaving it gives the side panel back to guidance.
            DisposableEffect(Unit) {
                onDispose { sideBarGuidanceInstructionConsumer.showCurrent() }
            }

            val scope = rememberCoroutineScope()

            val scrollState = rememberScrollState()
            // Where each row starts in the list (rows are as tall as their contents),
            // for the ↑ / ↓ items to move the list by one instruction.
            val rowTops = remember { mutableStateMapOf<Int, Int>() }

            Row {
                Column(
                    Modifier.weight(0.5F, true)
                ) {
                    SmoothScroll.MenuWindowSmoothScrollContext().SmoothScroll(
                        modifier = Modifier,
                        prependGoBackEntry = false,
                        tag = TAG,
                        items = listOf(
                            TextMenuItem(
                                title = "View Map",
                                onClicked = {
                                    navigationNodeTraverser.setResultAndGoBack(
                                        this@InGuidanceSubScreen,
                                        GuidanceSetupScreen.SubScreenResult.GO_BACK
                                    )
                                }
                            ),
                            TextMenuItem(
                                title = "Repeat current instruction",
                                onClicked = {
                                    scope.launch {
                                        guidanceInstructionConsumer.current.value?.let { (instruction, distance) ->
                                            guidanceInstructionConsumer.notifyInstruction(instruction, distance)
                                        }
                                    }

                                }
                            ),
                            TextMenuItem(
                                title = "Preview next instruction",
                                onClicked = { scope.launch { previewNextInstruction() } }
                            ),
                            TextMenuItem(
                                title = "↓ Instruction List",
                                onClicked = {
                                    scope.launch {
                                        rowTops.values.filter { it > scrollState.value }.minOrNull()
                                            ?.let { scrollState.animateScrollTo(it) }
                                    }
                                }
                            ),
                            TextMenuItem(
                                title = "↑ Instruction List",
                                onClicked = {
                                    scope.launch {
                                        scrollState.animateScrollTo(rowTops.values.filter { it < scrollState.value }.maxOrNull() ?: 0)
                                    }
                                }
                            )
                        ).map { it.toDynamicLambda()}
                    )
                }
                Column(Modifier.weight(0.5F)) {
                    InstructionList(
                        scrollState,
                        rowTops,
                        instructionItems = toShowInstructions.value.map {
                            instructionToListItem(it.first, it.second)
                        }
                    )
                }
            }

        }

    }


    /** The coming maneuver in the side panel, titled "Next"; until guidance's next update replaces it. */
    private suspend fun previewNextInstruction() {
        val (next, meters) = guidanceService.nextInstruction() ?: run {
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.MAP_GENERAL,
                "No more instructions",
                "The destination was the last one."
            ))
            return
        }
        val title = if (next.isDestination) "Next: Destination" else "Next  ${next.index + 1}/${next.count}"
        sideBarGuidanceInstructionConsumer.show(next, meters, title)
        delay(5.seconds)
        sideBarGuidanceInstructionConsumer.dismissInstruction(next)
    }

    @Composable
    fun InstructionList(
        scrollState : ScrollState,
        rowTops : MutableMap<Int, Int>,
        instructionItems : List<@Composable () -> Unit>
    ) {
        Box() {
            Column(Modifier.verticalScroll(scrollState)) {
                instructionItems.forEachIndexed { index, item ->
                    Box(Modifier.onGloballyPositioned { rowTops[index] = it.positionInParent().y.toInt() }) {
                        item()
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopEnd)
                ,
                adapter = ScrollbarAdapter(scrollState),
                style = ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 16.dp,
                    shape = RoundedCornerShape(8.dp),
                    hoverDurationMillis = 300,
                    unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                    hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                )
            )

        }
    }

    fun instructionToListItem(
        instruction : GuidanceInstruction,
        isCompleted : Boolean
    ) : @Composable () -> Unit = {
        if (!isCompleted) {
            Box(Modifier.fillMaxWidth()) {
                Notification(
                    image = GuidanceIcons.notificationImage(instruction.kind),
                    topText = if (instruction.streets.isNotEmpty()) "${instruction.streets}" else "",
                    contentText = instruction.text
                ).toView()
            }
        }
    }
}