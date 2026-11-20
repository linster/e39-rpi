package ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val gpsMapCenterProvider: GpsMapCenterProvider
) : NavigationNode<GuidanceSetupScreen.SubScreenResult> {
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
        //Have option to terminate guidance
        //Show map screen of next driving instruction
        //

        //TODO have a button here to temrinate guidance
        //TODO also put one in the guidance menu on the map.

        //TODO STEFAN 11-18-2026, need to finish this so that guidance works.
        // I also want to have an option to see the map with the route.

        // I want to see
        // the current instruction,
        // preview the next/prev instructions


        // Options to:
        // Center map on current GPS point
        // Terminate Guidance
        //
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("In Guidance")


            val instruction = guidanceInstructionConsumer.instructionFlow.collectAsState(null)

            // A preview is only for this screen: leaving it gives the side panel back to guidance.
            DisposableEffect(Unit) {
                onDispose { sideBarGuidanceInstructionConsumer.showCurrent() }
            }

            val scope = rememberCoroutineScope()
            key(gpsMapCenterProvider.followGps.value) {
                HalfScreenMenu.BottomHalfTwoColumn(
                    leftItems = listOf(
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
                            onClicked = {
                                scope.launch { previewNextInstruction() }
                            }
                        ),
                        CheckBoxMenuItem(
                            title = "Center map on current location?",
                            isChecked = gpsMapCenterProvider.followGps.value,
                            onCheckChanged = { gpsMapCenterProvider.followGps(it) }
                        ),
                    ),
                    rightItems = listOf()
                )
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
    }
}