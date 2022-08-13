package ca.stefanm.ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ca.stefanm.ibus.gui.map.poi.PoiSelectorScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.MapScreen
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSetupScreenInstructionConsumer
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
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

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        (params?.result as? SubScreenResult)?.let {
            if (it is SubScreenResult.GO_BACK) {
                navigationNodeTraverser.goBack()
            }
        }

        val sessionState = guidanceService.getGuidanceSessionState().collectAsState(null)

        LaunchedEffect(sessionState.value) {
            logger.d(TAG, "Session state is : ${sessionState.value}")
        }

        sessionState.value?.let {
            navigationNodeTraverser.navigateToNode(it.toScreenClass())
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