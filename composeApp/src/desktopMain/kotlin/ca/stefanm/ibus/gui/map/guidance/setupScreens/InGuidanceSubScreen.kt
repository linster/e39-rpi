package ca.stefanm.ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.stefanm.ca.stefanm.ibus.gui.docs.GuidanceScreenDocPartition
import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSetupScreenInstructionConsumer
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
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
    private val guidanceInstructionConsumer: GuidanceSetupScreenInstructionConsumer
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

        Column {
            BmwSingleLineHeader("InGuidance")


            val instruction = guidanceInstructionConsumer.instructionFlow.collectAsState(null)

            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(
                                this@InGuidanceSubScreen,
                                GuidanceSetupScreen.SubScreenResult.GO_BACK
                            )
                        }
                    )
                ),
                rightItems = listOf()
            )

        }

    }


}