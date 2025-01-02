package ca.stefanm.ibus.gui.map.mapScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.guidance.setupScreens.GuidanceSetupScreen
import ca.stefanm.ibus.gui.map.poi.CreateOrEditPoiScreen
import ca.stefanm.ibus.gui.map.poi.PoiManagerScreen
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.*
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject





@AutoDiscover
@ApplicationScope
class MapScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val logger : Logger,
    private val configurationStorage: ConfigurationStorage,
    private val poiRepository: PoiRepository,
    private val guidanceService: GuidanceService,
) : NavigationNode<MapScreenResult> {

    companion object {
        const val TAG = "MapScreen"

        //Open the map screen in such a way so that the user can select a point on the map.
        //When the point is selected, return a result
        fun openForUserLocationSelection(navigationNodeTraverser: NavigationNodeTraverser) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                MapScreen::class.java,
                MapScreenParameters(
                    persistUiStateOnClose = false,
                    openMode = MapScreenParameters.MapScreenOpenMode.LocationSelection(
                        center = LatLng(45.3154699,-75.9194058)
                    )
                )
            )
        }

        fun openForUserLocationSelection(
            navigationNodeTraverser: NavigationNodeTraverser,
            centerOn : LatLng
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                MapScreen::class.java,
                MapScreenParameters(
                    persistUiStateOnClose = false,
                    openMode = MapScreenParameters.MapScreenOpenMode.LocationSelection(
                        center = centerOn
                    )
                )
            )
        }

        fun openForBrowsingAtLocation(
            navigationNodeTraverser: NavigationNodeTraverser,
            centerOn: LatLng,
            clearBackStack : Boolean = false
        ) {
            if (clearBackStack) {
                navigationNodeTraverser.navigateToRoot()
            }
            navigationNodeTraverser.navigateToNodeWithParameters(
                MapScreen::class.java,
                MapScreenParameters(
                    persistUiStateOnClose = false,
                    usePersistedStateOnOpen = true,
                    openMode = MapScreenParameters.MapScreenOpenMode.BrowsingMode(
                        center = centerOn
                    )
                )
            )
        }
    }

    override val thisClass: Class<out NavigationNode<MapScreenResult>>
        get() = MapScreen::class.java

    private var browsingState : BrowsingState? = null
    data class BrowsingState(
        val extents: Extents,
        val mapOverlayState: MapOverlayState
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {


        val parameters = (it?.requestParameters as? MapScreenParameters) ?: MapScreenParameters(
            persistUiStateOnClose = true,
            openMode = MapScreenParameters.MapScreenOpenMode.BrowsingMode(
                center = configurationStorage.config[E39Config.MapConfig.defaultMapCenter].let {
                    LatLng(it.first, it.second)
                }
            )
        )



        val extents = remember { mutableStateOf(
            Extents(
            center = parameters.openMode.center.let { center -> GeoPosition(center.latitude, center.longitude) },
            mapScale = MapScale.KILOMETERS_1_6
        )
        ) }

        val currentCenter = remember { mutableStateOf(extents.value.center) }
        val currentOverlayState = remember { mutableStateOf<MapOverlayState>(MapOverlayState.NoOverlay) }


        val scope = rememberCoroutineScope()

        scope.launch() {
            knobListenerService.knobTurnEvents().collect { event ->

                logger.d(TAG, "collect turn $event, current state: ${currentOverlayState.value}")
                if (currentOverlayState.value == MapOverlayState.NoOverlay) {
                    if (event is InputEvent.NavKnobPressed) {
                        logger.d(TAG, "setting state to ModifyViewMenu")
                        currentOverlayState.value = MapOverlayState.ModifyViewMenu
                    }
                }

                if (currentOverlayState.value in listOf(
                        MapOverlayState.ChangeZoom,
                        MapOverlayState.PanUpDown,
                        MapOverlayState.PanLeftRight
                    )) {
                    if (event is InputEvent.NavKnobPressed) {
                        currentOverlayState.value = MapOverlayState.NoOverlay
                        modalMenuService.closeModalMenu()
                    }
                }

                if (currentOverlayState.value == MapOverlayState.ChangeZoom) {
                    if (event is InputEvent.NavKnobTurned) {
                        if (event.direction == InputEvent.NavKnobTurned.Direction.LEFT) {
                            //zoom out
                            -1
                        } else {
                            //zoom in
                            +1
                        }.let { change ->
                            val scales = MapScale.values().toList().sortedBy { it.meters }.circular()
                            val currentIndex = scales.indexOf(extents.value.mapScale) ?: 0
                            scales[currentIndex + change]
                        }.let { newZoom ->
                            extents.value = extents.value.copy(mapScale = newZoom)
                        }
                    }
                }

                if (currentOverlayState.value == MapOverlayState.PanLeftRight) {
                    if (event is InputEvent.NavKnobTurned) {
                        val direction = if (event.direction == InputEvent.NavKnobTurned.Direction.LEFT) {
                            LatLngTool.Bearing.WEST
                        } else {
                            LatLngTool.Bearing.EAST
                        }

                        extents.value = extents.value.copy(
                            center = ExtentCalculator.newMapCenterOnPan(
                                oldMapCenter = extents.value.center.let { LatLng(it.latitude, it.longitude) },
                                currentZoom = extents.value.mapScale,
                            bearing = direction).let { GeoPosition(it.latitude, it.longitude) })
                    }
                }

                if (currentOverlayState.value == MapOverlayState.PanUpDown) {
                    if (event is InputEvent.NavKnobTurned) {
                        val direction = if (event.direction == InputEvent.NavKnobTurned.Direction.LEFT) {
                            LatLngTool.Bearing.NORTH
                        } else {
                            LatLngTool.Bearing.SOUTH
                        }

                        extents.value = extents.value.copy(
                            center = ExtentCalculator.newMapCenterOnPan(
                                oldMapCenter = extents.value.center.let { LatLng(it.latitude, it.longitude) },
                                currentZoom = extents.value.mapScale,
                            bearing = direction).let { GeoPosition(it.latitude, it.longitude) })
                    }
                }
            }
        }


        LaunchedEffect(parameters.usePersistedStateOnOpen) {
            extents.value = browsingState?.extents ?: return@LaunchedEffect
            currentOverlayState.value = browsingState?.mapOverlayState ?: return@LaunchedEffect
        }

        DisposableEffect(currentOverlayState.value, extents.value) {
            browsingState = BrowsingState(
                extents = extents.value,
                mapOverlayState = currentOverlayState.value
            )
            onDispose {
                if (!parameters.persistUiStateOnClose) {
                    //Save the state by default, and clear it when the user doesn't
                    //want the state.
                    browsingState = null
                }

            }
        }


        LaunchedEffect(currentOverlayState.value) {
            logger.d(TAG, "Overlay State: ${currentOverlayState.value}")
            if (currentOverlayState.value == MapOverlayState.ModifyViewMenu) {
                showModifyViewMenu(
                    parameters.openMode,
                    currentCenterAcessor = { currentCenter.value.let { center -> LatLng(center.latitude, center.longitude) }},
                    onOverlayStateChanged = { currentOverlayState.value = it }
                )
            }
            if (currentOverlayState.value == MapOverlayState.GuidanceMenu) {
                showGuidanceMenu { currentOverlayState.value = it }
            }
            if (currentOverlayState.value == MapOverlayState.PoiMenu) {
                showPoiOverlaymenu(
                    currentCenter.value.let { LatLng(it.latitude, it.longitude) },
                    onOverlayStateChanged = { currentOverlayState.value = it }
                )
            }
        }



        MapViewer(
            overlayProperties = OverlayProperties(
                mapScaleVisible = currentOverlayState.value == MapOverlayState.ChangeZoom,
                centerCrossHairsVisible =
                    currentOverlayState.value == MapOverlayState.PanUpDown ||
                            currentOverlayState.value == MapOverlayState.PanLeftRight ||
                    parameters.openMode is MapScreenParameters.MapScreenOpenMode.LocationSelection,
                gpsReceptionIconVisible = false,
                poiOverlay = poiRepository.getAllVisiblePoisFlow().collectAsState(emptyList()).value.let { list ->
                    logger.d("POI LIST", list.toString())
                    PoiOverlay(
                        pois = list.map { poi ->
                            PoiOverlay.PoiOverlayItem(
                                label = poi.name,
                                position = poi.location,
                                icon = { Box{
                                    when (poi.icon) {
                                        is PoiRepository.Poi.PoiIcon.ColoredCircle -> PoiOverlay.PoiOverlayItem.CIRCLE_COLOR.invoke(poi.icon.color)
                                        is PoiRepository.Poi.PoiIcon.BundledIcon -> PoiOverlay.PoiOverlayItem.ICON_FILE.invoke(poi.icon.fileName, poi.icon.tint)
                                        else -> {}
                                    }
                                }
                                }
                            )
                        }
                    )
                },
                route = let {
                    //TODO this block infinte loops and I'm not sure why yet.
                    val sessionState = guidanceService.getGuidanceSessionState().collectAsState(GuidanceSession.SessionState.SETTING_UP)
                    logger.d(TAG, "Guidance session state: $sessionState")
                    if (sessionState.value == GuidanceSession.SessionState.IN_GUIDANCE) {
                        val session = guidanceService.getCurrentSessionInstantaneous()
                        val route = session.route ?: listOf()
                        LaunchedEffect(route) {
                            logger.d(TAG, "Route changed: ${route.size}")
                        }
                        Route(
                            path = route,
                            color = Color.Magenta,
                            stroke = Stroke(8F)
                        )
                        null
                    } else {
                        null //Don't draw a route
                    }
                }
            ),
            extents = extents.value,
            onCenterPositionUpdated = {
                currentCenter.value = it
            }
        )

    }

    private fun showModifyViewMenu(
        mapScreenOpenMode: MapScreenParameters.MapScreenOpenMode,
        currentCenterAcessor : () -> LatLng,
        onOverlayStateChanged : (new : MapOverlayState) -> Unit,
    ) {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 384,
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = true,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    onOverlayStateChanged(MapOverlayState.NoOverlay)
                },
                items = let {
                    val zoomControls = listOf(
                        ModalMenu.ModalMenuItem(
                            title = "Zoom",
                            onClicked = {
                                onOverlayStateChanged(MapOverlayState.ChangeZoom)
                            }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬌",
                            onClicked = { onOverlayStateChanged(MapOverlayState.PanLeftRight) }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬍",
                            onClicked = { onOverlayStateChanged(MapOverlayState.PanUpDown) }
                        )
                    )

                    val browsingEntries = if (mapScreenOpenMode is MapScreenParameters.MapScreenOpenMode.BrowsingMode) {
                        listOf(
                            ModalMenu.ModalMenuItem(
                                title = "Guidance",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    onOverlayStateChanged(MapOverlayState.GuidanceMenu)
                                }
                            ),
                            ModalMenu.ModalMenuItem(
                                title = "POIs",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    onOverlayStateChanged(MapOverlayState.PoiMenu)
                                }
                            )
                        )
                    } else { listOf() }

                    val closingEntries = when (mapScreenOpenMode) {
                        is MapScreenParameters.MapScreenOpenMode.BrowsingMode -> {
                            listOf(
                                ModalMenu.ModalMenuItem(
                                    title = "Main Menu",
                                    onClicked = {
                                        navigationNodeTraverser.navigateToRoot()
                                    }
                                )
                            )
                        }
                        is MapScreenParameters.MapScreenOpenMode.LocationSelection -> {
                            listOf(
                                ModalMenu.ModalMenuItem(
                                    title = "Go Back",
                                    onClicked = {
                                        modalMenuService.closeModalMenu()
                                        navigationNodeTraverser.setResultAndGoBack(
                                            this,
                                            MapScreenResult.PointSelectedResult(null)
                                        )
                                    }
                                ),
                                ModalMenu.ModalMenuItem(
                                    title = "Select Center",
                                    onClicked = {
                                        modalMenuService.closeModalMenu()
                                        navigationNodeTraverser.setResultAndGoBack(
                                            this,
                                            MapScreenResult.PointSelectedResult(
                                                currentCenterAcessor()
                                            )
                                        )
                                    }
                                )
                            )
                        }
                        else -> { listOf() }
                    }


                    zoomControls + browsingEntries + closingEntries
                }
            )
        )
    }
    
    private fun showGuidanceMenu(onOverlayStateChanged : (new : MapOverlayState) -> Unit) {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512,
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = false,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                },
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Back",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = let{
                            if (guidanceService.getInstantaneousGuidanceSessionState() != GuidanceSession.SessionState.IN_GUIDANCE) {
                                "Setup..."
                            } else {
                                "Direction Options"
                            }
                       },
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            navigationNodeTraverser.navigateToNode(GuidanceSetupScreen::class.java)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Terminate Guidance",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            guidanceService.stopGuidance()
                            onOverlayStateChanged(MapOverlayState.NoOverlay)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Repeat Direction",
                        onClicked = {
                            guidanceService.repeatLastDirection()
                        }
                    )
                )
            )
        )
    }

    private fun showPoiOverlaymenu(
        currentCenter: LatLng,
        onOverlayStateChanged : (new : MapOverlayState) -> Unit
    ) {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = false,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                },
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Back",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Open Address Book...",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            navigationNodeTraverser.navigateToNode(PoiManagerScreen::class.java)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "New POI on Center",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            CreateOrEditPoiScreen.newPoiAtLocation(
                                navigationNodeTraverser,
                                currentCenter
                            )
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Hide All POIs",
                        onClicked = {
                            poiRepository.hideAllPois()
                            modalMenuService.closeModalMenu()
                            onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Show All Stored POIs",
                        onClicked = {
                            poiRepository.showAllPois()
                            modalMenuService.closeModalMenu()
                            onOverlayStateChanged(MapOverlayState.ModifyViewMenu)
                        }
                    )
                )
            )
        )
    }




}