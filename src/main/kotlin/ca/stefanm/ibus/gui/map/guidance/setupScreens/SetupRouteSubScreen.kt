package ca.stefanm.ibus.gui.map.guidance.setupScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.map.poi.PoiSelectorScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
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
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AutoDiscover
class SetupRouteSubScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val configurationStorage: ConfigurationStorage,
    private val guidanceService: GuidanceService,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger
) : NavigationNode<GuidanceSetupScreen.SubScreenResult> {

    companion object {
        private const val TAG = "SetupRouteSubScreen"
    }

    override val thisClass: Class<out NavigationNode<GuidanceSetupScreen.SubScreenResult>> = SetupRouteSubScreen::class.java


    enum class LocationResponseFor { START_LOCATION, END_LOCATION }
    private var locationResponseFor : LocationResponseFor? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        LaunchedEffect(params) {
            val mapSelectionResult = if (params?.resultFrom == MapScreen::class.java && params.result is MapScreenResult) {
                (params.result as? MapScreenResult.PointSelectedResult)?.point
            } else { null }

            val poiSelectionResult = if (params?.resultFrom == PoiSelectorScreen::class.java && params.result is PoiSelectorScreen.PoiSelectionResult) {
                (params.result as? PoiSelectorScreen.PoiSelectionResult.PoiSelected)?.poi?.location
            } else null

            val selectionResult = mapSelectionResult ?: poiSelectionResult

            if (selectionResult != null) {
                when (locationResponseFor) {
                    LocationResponseFor.START_LOCATION -> {
                        guidanceService.setStartPoint(selectionResult)
                    }
                    LocationResponseFor.END_LOCATION -> {
                        guidanceService.setEndPoint(selectionResult)
                    }
                    null -> {
                        logger.w(TAG, "We have a map selection result $mapSelectionResult but no locationResponseFor??")
                    }
                }
            }
        }

        val currentSession = guidanceService.getCurrentSession().collectAsState(GuidanceSession())
        val sessionState = guidanceService.getGuidanceSessionState().collectAsState(null)

        LaunchedEffect(sessionState.value) {
            logger.d(TAG, "Session state is : ${sessionState.value}")
        }

        when (sessionState.value) {
            GuidanceSession.SessionState.SETTING_UP,
            GuidanceSession.SessionState.READY_TO_CALCULATE,
            GuidanceSession.SessionState.ROUTE_CALCULATED -> SetupRouteScreen(currentSession.value)
            GuidanceSession.SessionState.IN_GUIDANCE,
            GuidanceSession.SessionState.TERMINATED -> {
                navigationNodeTraverser.setResultAndGoBack(
                    this, GuidanceSetupScreen.SubScreenResult.STATE_CHANGED
                )
            }
            else -> {}
        }


    }


    @Composable
    fun SetupRouteScreen(session: GuidanceSession) {
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Guidance Setup")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.setResultAndGoBack(this@SetupRouteSubScreen, GuidanceSetupScreen.SubScreenResult.GO_BACK) }
                    ),
                    TextMenuItem(
                        title = "${checkBoxIfLocationNotNull { session.startPoint } }Set Start Location",
                        isSelectable = session.sessionState in listOf(
                            GuidanceSession.SessionState.SETTING_UP,
                            GuidanceSession.SessionState.READY_TO_CALCULATE
                        )
                    ) {
                        openLocatonChooser(
                            title = "Set Start Location",
                            { session.startPoint ?: getDefaultMapSelectionLocation() },
                            LocationResponseFor.START_LOCATION
                        )
                    },
                    TextMenuItem(
                        title = "${checkBoxIfLocationNotNull { session.endPoint } }Set End Location",
                        isSelectable = session.sessionState in listOf(
                            GuidanceSession.SessionState.SETTING_UP,
                            GuidanceSession.SessionState.READY_TO_CALCULATE
                        )
                    ) {
                        openLocatonChooser("Set End Location",
                            { session.endPoint ?: getDefaultMapSelectionLocation() },
                            LocationResponseFor.END_LOCATION
                        )
                    }
                ) + when (session.sessionState) {
                    GuidanceSession.SessionState.SETTING_UP -> {
                        listOf(
                            TextMenuItem(
                                title = "Calculate Route",
                            ) {
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.ALERT_CIRCLE,
                                    topText = "Cannot Calculate Route",
                                    contentText = "Route is missing information."
                                ))
                            }
                        )
                    }
                    GuidanceSession.SessionState.READY_TO_CALCULATE -> {
                        listOf(
                            TextMenuItem(title = "Calculate Route") {
                                calculateRoute()
                            }
                        )
                    }
                    GuidanceSession.SessionState.ROUTE_CALCULATED -> {
                        listOf(
                            TextMenuItem(title = "Clear Route") {
                                guidanceService.clearCurrentSession()
                            },
                            TextMenuItem(title = "Start Guidance") {
                                startGuidance()
                            }
                        )
                    }
                    else -> {
                        listOf(
                            TextMenuItem(
                                title = "Clear Route Setup",
                                onClicked = {
                                    guidanceService.clearCurrentSession()
                                    navigationNodeTraverser.setResultAndGoBack(this@SetupRouteSubScreen, GuidanceSetupScreen.SubScreenResult.GO_BACK)
                                }
                            ),
                        )
                    }
                }
            )
        }
    }

    fun openLocatonChooser(
        title : String,
        existingLocationAccessor : () -> LatLng,
        locationResponseFor: LocationResponseFor
    ) {
        //Side-bar to show current point,
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.LatLngDetailSidePanelMenu(
                title = title,
                poi = existingLocationAccessor().let {
                    PoiRepository.Poi(
                        name = "Start",
                        location = it,
                        PoiRepository.Poi.PoiIcon.ColoredCircle(Color.Red),
                        isVisible = true
                    )
                },
                centerCrossHairsVisible = true,
                mapScale = MapScale.METERS_400,
                buttons = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            this.locationResponseFor = null
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    ),
                    TextMenuItem(
                        title = "Select on Map",
                        onClicked = {
                            this.locationResponseFor = locationResponseFor
                            modalMenuService.closeSidePaneOverlay(true)
                            MapScreen.openForUserLocationSelection(navigationNodeTraverser)
                        }
                    ),
                    TextMenuItem(
                        title = "Select from Address Book",
                        onClicked = {
                            this.locationResponseFor = locationResponseFor
                            modalMenuService.closeSidePaneOverlay(true)
                            navigationNodeTraverser.navigateToNode(PoiSelectorScreen::class.java)
                        }
                    )
                )
            )
        }
    }


    private fun calculateRoute() {
        guidanceService.calculateRoute()
    }


    private fun startGuidance() {
        if (guidanceService.getInstantaneousGuidanceSessionState() == GuidanceSession.SessionState.IN_GUIDANCE) {
            //
        }
        guidanceService.startGuidance()
        navigationNodeTraverser.setResultAndGoBack(
            this@SetupRouteSubScreen,
            GuidanceSetupScreen.SubScreenResult.STATE_CHANGED
        )
    }

    private fun checkBoxIfLocationNotNull(accessor : () -> LatLng?) : String {
        return if (accessor() != null) { "✓ " } else { ""}
    }

    private fun getDefaultMapSelectionLocation() : LatLng {
        return configurationStorage.config[E39Config.MapConfig.defaultMapCenter].let { LatLng(it.first, it.second) }
    }
}