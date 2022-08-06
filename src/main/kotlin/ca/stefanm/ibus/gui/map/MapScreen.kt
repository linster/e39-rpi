package ca.stefanm.ibus.gui.map

import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ca.stefanm.ibus.gui.map.poi.PoiManagerScreen
import ca.stefanm.ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
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
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val poiRepository: PoiRepository
) : NavigationNode<MapScreen.MapScreenResult> {

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

    sealed class MapScreenResult {
        data class PointSelectedResult(val point : LatLng?) : MapScreenResult()
    }

    override val thisClass: Class<out NavigationNode<MapScreenResult>>
        get() = MapScreen::class.java

    private data class MapScreenParameters(
        /* If true, store the map zoom and center state so it can be reused on the next open */
        val persistUiStateOnClose : Boolean = false,
        /* If true, use the stored map zoom and center state when opening */
        val usePersistedStateOnOpen : Boolean = false,

        val openMode : MapScreenOpenMode
    ) {
        sealed class MapScreenOpenMode(
            open val center : LatLng
        ) {
            /* The user is just browsing around on the map */
            data class BrowsingMode(
                override val center : LatLng,
                val persistedState: BrowsingState? = null,
            ) : MapScreenOpenMode(center = center)
            data class LocationSelection(override val center : LatLng) : MapScreenOpenMode(center = center)
        }
    }

    private var browsingState : BrowsingState? = null
    private data class BrowsingState(
        val extents: Extents,
        val mapOverlayState: MapOverlayState
    )

    private sealed interface MapOverlayState {
        object NoOverlay : MapOverlayState
        object ModifyViewMenu : MapOverlayState
        object GuidanceMenu : MapOverlayState
        object PoiMenu : MapOverlayState
        object PanLeftRight : MapOverlayState
        object PanUpDown : MapOverlayState
        object ChangeZoom : MapOverlayState
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {


        val parameters = (it?.requestParameters as? MapScreenParameters) ?: MapScreenParameters(
            persistUiStateOnClose = true,
            openMode = MapScreenParameters.MapScreenOpenMode.BrowsingMode(
                center = configurationStorage.config[E39Config.MapConfig.defaultMapCenter].let {
                    LatLng(it.first, it.second)
                }
            )
        )



        val extents = remember { mutableStateOf(Extents(
            center = parameters.openMode.center.let { center -> GeoPosition(center.latitude, center.longitude) },
            mapScale = MapScale.KILOMETERS_1_6
        )) }

        val currentCenter = remember { mutableStateOf(extents.value.center) }

        val currentOverlayState = remember { mutableStateOf<MapOverlayState>(MapOverlayState.NoOverlay) }
        //Adapter because modalMenu doesn't have Composable on-Click methods.
        val currentOverlayStateBus = MutableStateFlow(currentOverlayState.value)
        rememberCoroutineScope().launch {
            currentOverlayStateBus.collect { currentOverlayState.value = it }
        }


        LaunchedEffect(currentOverlayState.value) {
            knobListenerService.knobTurnEvents().collect { event ->

                logger.d(TAG, "collect turn $event")
                if (currentOverlayState.value == MapOverlayState.NoOverlay) {
                    if (event is InputEvent.NavKnobPressed) {
                        currentOverlayState.value = MapOverlayState.ModifyViewMenu
                    }
                }

                if (currentOverlayState.value in listOf(MapOverlayState.ChangeZoom, MapOverlayState.PanUpDown, MapOverlayState.PanLeftRight)) {
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
            currentOverlayStateBus.value = browsingState?.mapOverlayState ?: return@LaunchedEffect
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
                    currentOverlayStateBus,
                    parameters.openMode,
                    currentCenterAcessor = { currentCenter.value.let { center -> LatLng(center.latitude, center.longitude) }}
                )
            }
            if (currentOverlayState.value == MapOverlayState.GuidanceMenu) {
                showGuidanceMenu(currentOverlayStateBus)
            }
            if (currentOverlayState.value == MapOverlayState.PoiMenu) {
                showPoiOverlaymenu(currentOverlayStateBus)
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
                poiOverlay = poiRepository.getAllPoisFlow().collectAsState(emptyList()).value.let { list ->
                    logger.d("POI LIST", list.toString())
                    PoiOverlay(
                        pois = list.map { poi ->
                            PoiOverlay.PoiOverlayItem(
                                label = poi.name,
                                position = poi.location,
                                icon = PoiRepository.Poi.toViewForMapScreen(poi.icon)
                            )
                        }
                    )
                }
            ),
            extents = extents.value,
            onCenterPositionUpdated = {
                currentCenter.value = it
            }
        )

    }

    private fun showModifyViewMenu(
        currentOverlayStateBus : MutableStateFlow<MapOverlayState>,
        mapScreenOpenMode: MapScreenParameters.MapScreenOpenMode,
        currentCenterAcessor : () -> LatLng
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
                    currentOverlayStateBus.value = MapOverlayState.NoOverlay
                },
                items = let {
                    val zoomControls = listOf(
                        ModalMenu.ModalMenuItem(
                            title = "Zoom",
                            onClicked = {
                                currentOverlayStateBus.value = MapOverlayState.ChangeZoom
                            }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬌",
                            onClicked = { currentOverlayStateBus.value = MapOverlayState.PanLeftRight }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬍",
                            onClicked = { currentOverlayStateBus.value = MapOverlayState.PanUpDown }
                        )
                    )

                    val browsingEntries = if (mapScreenOpenMode is MapScreenParameters.MapScreenOpenMode.BrowsingMode) {
                        listOf(
                            ModalMenu.ModalMenuItem(
                                title = "Guidance",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    currentOverlayStateBus.value = MapOverlayState.GuidanceMenu
                                }
                            ),
                            ModalMenu.ModalMenuItem(
                                title = "POIs",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    currentOverlayStateBus.value = MapOverlayState.PoiMenu
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
    
    private fun showGuidanceMenu(currentOverlayStateBus : MutableStateFlow<MapOverlayState>) {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512,
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = true,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
                },
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Back",
                        onClicked = {
                            currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Enter Address",
                        onClicked = { currentOverlayStateBus.value = MapOverlayState.PanLeftRight }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Terminate Guidance",
                        onClicked = { currentOverlayStateBus.value = MapOverlayState.PanUpDown }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Repeat Direction",
                        onClicked = {

                        }
                    )
                )
            )
        )
    }

    private fun showPoiOverlaymenu(currentOverlayStateBus: MutableStateFlow<MapOverlayState>) {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = false,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
                },
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Back",
                        onClicked = {
                            currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
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
                        title = "Hide All POIs",
                        onClicked = {
                            poiRepository.hideAllPois()
                            modalMenuService.closeModalMenu()
                            currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Show All Stored POIs",
                        onClicked = {
                            poiRepository.showAllPois()
                            modalMenuService.closeModalMenu()
                            currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu
                        }
                    )
                )
            )
        )
    }




}