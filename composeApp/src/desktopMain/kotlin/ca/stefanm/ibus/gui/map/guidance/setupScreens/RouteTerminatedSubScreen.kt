package ca.stefanm.ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import ca.stefanm.ca.stefanm.ibus.gui.docs.GuidanceScreenDocPartition
import ca.stefanm.gui.map.guidance.BrowsableRouteStorage
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.Extents
import ca.stefanm.ibus.gui.map.MapViewer
import ca.stefanm.ibus.gui.map.OverlayProperties
import ca.stefanm.ibus.gui.map.Route
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.flow.map
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject

@ScreenDoc(
    screenName = "RouteTerminatedSubScreen",
    description = "When a route is terminated (user cancelled a guidance session), this screen" +
            "is visible to allow the user to cancel the session.",
    navigatesTo = [
        ScreenDoc.NavigateTo(GuidanceSetupScreen::class)
    ]
)

@GuidanceScreenDocPartition
@AutoDiscover
class RouteTerminatedSubScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val guidanceService: GuidanceService,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService,
    private val browsableRouteStorage: BrowsableRouteStorage
) : NavigationNode<GuidanceSetupScreen.SubScreenResult> {

    override val thisClass: Class<out NavigationNode<GuidanceSetupScreen.SubScreenResult>>
        get() = RouteTerminatedSubScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        //Offer to save the route to replay storage.
        //Show a big map of where the route was.


        val currentSession = guidanceService.getGuidanceSessionState().collectAsState(null)

        when (currentSession.value) {
            GuidanceSession.SessionState.SETTING_UP,
            GuidanceSession.SessionState.READY_TO_CALCULATE,
            GuidanceSession.SessionState.ROUTE_CALCULATED,
            GuidanceSession.SessionState.IN_GUIDANCE -> {
                navigationNodeTraverser.setResultAndGoBack(
                    this, GuidanceSetupScreen.SubScreenResult.STATE_CHANGED
                )
            }
            GuidanceSession.SessionState.TERMINATED -> RouteTerminatedScreen()
            else -> {}
        }
    }

    @Composable
    private fun RouteTerminatedScreen() {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            BmwSingleLineHeader("Guidance Terminated")

            val route = guidanceService.getCurrentSession().map { it.route }.collectAsState(listOf())

            route.value?.let { points ->
                if (points.isNotEmpty()) {
                    Box(
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        MapViewer(
                            overlayProperties = OverlayProperties(
                                mapScaleVisible = true,
                                route = Route(points, Color.Magenta, Stroke(8F)),
                                centerCrossHairsVisible = false,
                                gpsReceptionIconVisible = false
                            ),
                            extents = Extents(
                                center = Route.findCenter(points).let { GeoPosition(it.latitude, it.longitude) },
                                mapScale = Route.findMapScaleForOverview(points)
                            ),
                            onCenterPositionUpdated = {}
                        )
                    }
                } else {
                    Spacer(Modifier.fillMaxHeight())
                }
            }


            HalfScreenMenu.BottomHalfTwoColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser
                            .setResultAndGoBack(this@RouteTerminatedSubScreen,
                                GuidanceSetupScreen.SubScreenResult.GO_BACK)
                        }
                    )
                ),
                listOf(
                    TextMenuItem(
                        title = "Start New Route...",
                        onClicked = {
                            guidanceService.clearCurrentSession()
                        }
                    ),
                    TextMenuItem(
                        title = "Save Route for Browsing",
                        onClicked = {
                            notificationHub.postNotificationBackground(Notification(
                                Notification.NotificationImage.NONE,
                                "Enter Route Name"
                            ))
                            modalMenuService.showKeyboard(
                                Keyboard.KeyboardType.FULL,
                                browsableRouteStorage.suggestRouteName()
                            ) { enteredName ->
                                route.value?.let { points ->
                                    browsableRouteStorage.saveRoute(enteredName, Route(path = points, Color.Magenta, Stroke(8F)))
                                }
                            }

                        }
                    )
                )
            )
        }
    }
}