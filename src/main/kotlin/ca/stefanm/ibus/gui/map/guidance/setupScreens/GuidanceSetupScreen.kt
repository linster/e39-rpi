package ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
import javax.inject.Inject

@AutoDiscover
class GuidanceSetupScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val guidanceService: GuidanceService,
    private val logger: Logger
) : NavigationNode<Nothing> {

    companion object {
        private const val TAG = "GuidanceSetupScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = GuidanceSetupScreen::class.java

    sealed interface SubScreenResult {
        object GO_BACK : SubScreenResult //Guidance Setup screen will then also go back out.
        object STATE_CHANGED : SubScreenResult
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ { params ->
        BmwSingleLineHeader("Guidance Setup")

        LaunchedEffect(Unit) {
            val result = params?.result

            if (result == null) {
                val instantaneousState = guidanceService.getInstantaneousGuidanceSessionState()
                navigationNodeTraverser.navigateToNode(instantaneousState.toScreenClass())
            } else {
                if (result is SubScreenResult.GO_BACK) {
                    logger.d(TAG, "Going Back!")
                    delay(5)
                    navigationNodeTraverser.goBack()
                }
                if (result is SubScreenResult.STATE_CHANGED) {
                    val instantaneousState = guidanceService.getInstantaneousGuidanceSessionState()
                    navigationNodeTraverser.navigateToNode(instantaneousState.toScreenClass())
                }
            }
        }
    }

    fun GuidanceSession.SessionState.toScreenClass() : Class<out NavigationNode<SubScreenResult>> = when(this) {
        GuidanceSession.SessionState.SETTING_UP,
        GuidanceSession.SessionState.READY_TO_CALCULATE,
        GuidanceSession.SessionState.ROUTE_CALCULATED -> {
            SetupRouteSubScreen::class.java
        }
        GuidanceSession.SessionState.IN_GUIDANCE -> {
            InGuidanceSubScreen::class.java
        }
        GuidanceSession.SessionState.TERMINATED -> {
            RouteTerminatedSubScreen::class.java
        }
    }

}