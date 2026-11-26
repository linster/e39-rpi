package ca.stefanm.ibus.gui.map.mapScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.setupScreens.BackgroundGuidanceSetupTool
import ca.stefanm.ca.stefanm.ibus.gui.map.widget.GpsReceptionOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenOpener
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.guidance.setupScreens.GuidanceSetupScreen
import ca.stefanm.ibus.gui.map.poi.CreateOrEditPoiScreen
import ca.stefanm.ibus.gui.map.poi.PoiManagerScreen
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.map.places.PlacesNearbyScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.apps.pdfViewer.PdfViewerScreen
import ca.stefanm.ibus.gui.map.*
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
import ca.stefanm.ibus.gui.map.places.MapPoi
import ca.stefanm.ibus.gui.map.places.MapPoiLabels
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.ZoomCalculator
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarController
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject
import javax.inject.Named


@AutoDiscover
@ApplicationScope
class MapScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,

    private val logger : Logger,
    private val configurationStorage: ConfigurationStorage,
    private val poiRepository: PoiRepository,
    private val guidanceService: GuidanceService,
    private val gpsMapCenterProvider: GpsMapCenterProvider,
    private val bottomBarController: BottomBarController,
    private val backgroundGuidanceSetupTool: BackgroundGuidanceSetupTool,
    private val locationRepository: LocationRepository
) : NavigationNode<MapScreenResult> {

    companion object : MapScreenOpener {
        const val TAG = "MapScreen"
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
        ).also { parameters -> logger.d(TAG, "Map Screen Parameters: $parameters") }

        val shouldFollowGps by gpsMapCenterProvider.followGps.collectAsState(false)


        var extents = remember {
            Extents(
                center = parameters.openMode.center,
                mapScale = MapScale.KILOMETERS_1_6
            )
        }

        if (parameters.openMode is MapScreenParameters.MapScreenOpenMode.BrowsingMode) {
            LaunchedEffect(shouldFollowGps) {
                if (shouldFollowGps) {
                    gpsMapCenterProvider.center.collect { gpsCenter ->
                        extents.centerState = gpsCenter
                    }
                }
            }
        }

        val currentOverlayState = remember { mutableStateOf<MapOverlayState>(MapOverlayState.NoOverlay) }


        // Once per visit to the map (cancelled when it's left): launched
        // straight in the composable body, every recomposition added another
        // collector, so each knob event was handled several times (a press
        // opened the menu and the next one picked "Zoom" or closed it).
        LaunchedEffect(Unit) {
            knobListenerService.knobTurnEvents()
                .onStart {
                    logger.d(TAG, "knobTurnEvents() flow started")
                }
                .onCompletion { cause ->
                    logger.d(TAG, "knobTurnEvents() flow completed. cause: ${cause}")
                }
                .collect { event ->

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
                            val currentIndex = scales.indexOf(extents.mapScaleState) ?: 0
                            scales[currentIndex + change]
                        }.let { newZoom ->
                            extents.mapScaleState = newZoom
                            // This is the only spot the onUserMapCenterChanged learns about new zooms. It learns about
                            // new centers through the map widget's callback.
                            gpsMapCenterProvider.onUserMapCenterChanged(extents.centerState, extents.mapScaleState )
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

                        extents.centerState = ExtentCalculator.newMapCenterOnPan(
                            oldMapCenter = extents.centerState,
                            currentZoom = extents.mapScaleState,
                            bearing = direction)
                        //gpsMapCenterProvider.onUserMapCenterChanged(extents.centerState, extents.mapScaleState )
                    }
                }

                if (currentOverlayState.value == MapOverlayState.PanUpDown) {
                    if (event is InputEvent.NavKnobTurned) {
                        val direction = if (event.direction == InputEvent.NavKnobTurned.Direction.LEFT) {
                            LatLngTool.Bearing.NORTH
                        } else {
                            LatLngTool.Bearing.SOUTH
                        }

                        extents.centerState = ExtentCalculator.newMapCenterOnPan(
                            oldMapCenter = extents.centerState,
                            currentZoom = extents.mapScaleState,
                            bearing = direction)
                        //gpsMapCenterProvider.onUserMapCenterChanged(extents.centerState, extents.mapScaleState )
                    }
                }
            }
        }


        LaunchedEffect(parameters.usePersistedStateOnOpen) {
            logger.d(TAG, "usePersistedStateOnOpen: ${parameters.usePersistedStateOnOpen}")
            logger.d(TAG, "usePersistedStateOnOpen launched effect. $browsingState")
            extents = browsingState?.extents ?: return@LaunchedEffect
            currentOverlayState.value = browsingState?.mapOverlayState ?: return@LaunchedEffect
        }

        DisposableEffect(currentOverlayState.value, extents) {
            //TODO does this get updated when extents zoom and center changes too?
            logger.d("WAT", "Browsing state update triggered")
            browsingState = BrowsingState(
                extents = extents,
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


        // Enable the gps marker when any of the gps services are running.
        val anyLocationServiceRunning by remember {
            locationRepository.anyLocationServiceRunning()
        }.collectAsState(false)

        val currentlySelectedPoi = remember { mutableStateOf<MapPoi?>(null) }

        LaunchedEffect(currentOverlayState.value) {
            logger.d(TAG, "Overlay State: ${currentOverlayState.value}")

            if (currentOverlayState.value == MapOverlayState.ModifyViewMenu) {
                showModifyViewMenu(
                    parameters.openMode,
                    currentCenterAcessor = { extents.centerState },
                    currentPoiAccessor = { currentlySelectedPoi.value },
                    onOverlayStateChanged = { new -> currentOverlayState.value = new },
                    isLocationServiceRunningAccessor = { anyLocationServiceRunning },
                    onRequestRecenterToLocation = {
                        val currentPosition = locationRepository.getPositionStateFlow().value
                        if (currentPosition != null) {
                            extents.centerState = currentPosition
                        }
                    }
                )
            }
            if (currentOverlayState.value == MapOverlayState.GuidanceMenu) {
                showGuidanceMenu { new -> currentOverlayState.value = new }
            }
            if (currentOverlayState.value == MapOverlayState.PoiMenu) {
                showPoiOverlaymenu(
                    currentCenter = extents.centerState,
                    onOverlayStateChanged = { currentOverlayState.value = it }
                )
            }
        }

        val poiList by poiRepository.getAllVisiblePoisFlow().collectAsState(emptyList())


        val gpsMarker : State<PoiOverlay.PoiOverlayItem?> = locationRepository.getPosition().map { latLng ->
            if (latLng != null && anyLocationServiceRunning) {
                PoiOverlay.PoiOverlayItem(
                    label = "GPS Location",
                    position = latLng,
                    icon = { PoiOverlay.PoiOverlayItem.CIRCLE_COLOR.invoke(Color.Cyan) }
                )
            } else { null }
        }.collectAsState(null)

        val routeLatLng by guidanceService.getSessionRoute().collectAsState(null)

        val selectablePois = SnapshotStateList<PoiOverlay.PoiOverlayItem>()


        var viewPortWidth by remember { mutableStateOf(0.dp) }
        var viewPortHeight by remember { mutableStateOf(0.dp) }

        if (parameters.openMode is MapScreenParameters.MapScreenOpenMode.PoiBrowser) {
            var poiListenerIsEnabled by remember { mutableStateOf(false) }
            val knobStatePois = KnobObserverBuilderState.setupEnableableListener(
                knobListenerService = knobListenerService,
                isEnabled = poiListenerIsEnabled,
                logger = logger,
                tag = TAG
            )

            val bottombarInfo = remember {  MutableStateFlow(BottomBarController.BottomBarViewState.MapPoiSelection.PoiSelectionInfo(
                0, "None1", 0, 0
            )) }

            val poisExistOnScreen by remember { derivedStateOf { selectablePois.isNotEmpty() } }
            DisposableEffect(poisExistOnScreen) {
                bottomBarController.bottomBarViewState.value = BottomBarController.BottomBarViewState.MapPoiSelection(
                    mapPoiSelectionInfo = bottombarInfo.asStateFlow()
                )
                onDispose {
                    bottomBarController.bottomBarViewState.value = BottomBarController.BottomBarViewState.DateTime
                }
            }

            //TODO this seems like an anti-pattern... maybe have a remember(key)?
            LaunchedEffect(currentOverlayState.value) {
                // Only enable the knob listener when none of the overlays are visible
                val knobListenerShouldBeEnabled = when (currentOverlayState.value) {
                    MapOverlayState.ChangeZoom,
                    MapOverlayState.GuidanceMenu,
                    MapOverlayState.ModifyViewMenu,
                    MapOverlayState.PanLeftRight,
                    MapOverlayState.PanUpDown,
                    MapOverlayState.PoiMenu -> false
                    MapOverlayState.NoOverlay -> true
                }
                poiListenerIsEnabled = knobListenerShouldBeEnabled
            }

            // TODO I think this needs a launched effect here

            selectablePois.clear()
            selectablePois.addAll(buildList {
                //TODO, filter by what's visible in the map view.

                val list = parameters.openMode.poiById.values

                bottombarInfo.value = bottombarInfo.value.copy(
                    resultCount = list.size
                )


                //TODO filter the list here by whether the poi fits on the map

                val filteredList = remember(extents.centerState, extents.mapScaleState) {
                    list.filter {
                        ZoomCalculator.isLatLngInZoom(
                            it.location,
                            extents,
                            viewportWidth = viewPortWidth,
                            viewportHeight = viewPortHeight
                        )
                    }.also {
                        bottombarInfo.value = bottombarInfo.value.copy(
                            totalIndices = it.size
                        )
                    }
                }

                filteredList.forEachIndexed { index, poi ->
                    add(PoiOverlay.PoiOverlayItem(
                        label = poi.name,
                        position = poi.location,
                        icon = @Composable {
                            KnobObserverBuilder(knobStatePois) { allocatedIndex, currentIndex ->
                                LaunchedEffect(allocatedIndex, currentIndex) {
                                    if (currentIndex == allocatedIndex) {
                                        bottombarInfo.value = bottombarInfo.value.copy(
                                            currentIndex = index,
                                            currentItemName = poi.name,
                                        )
                                        // Set the current POI so that the modify view menu can use it to
                                        // open the details pane.
                                        currentlySelectedPoi.value = poi
                                    }
                                }
                                PoiOverlay.PoiOverlayItem.SelectablePoi(
                                    poi,
                                    index.toString(),
                                    isPreselected = poi.id == parameters.openMode.preSelected?.poi?.id
                                ) {
                                    poiListenerIsEnabled = false
                                    showSelectablePoiSideMenu(poi, parameters.openMode) {
                                        currentOverlayState.value = it
                                    }
                                }.invoke(this, allocatedIndex, currentIndex)
                            }
                        }
                    ))
                }
            })
        }

        MapViewer(
            overlayProperties = OverlayProperties(
                mapScaleVisible = currentOverlayState.value == MapOverlayState.ChangeZoom,
                centerCrossHairsVisible =
                    currentOverlayState.value in listOf(MapOverlayState.PanUpDown,  MapOverlayState.PanLeftRight) ||
                    parameters.openMode is MapScreenParameters.MapScreenOpenMode.LocationSelection,
                gpsReceptionIconVisible = anyLocationServiceRunning,
                gpsReceptionOverlayState = GpsReceptionOverlayState(
                    state = locationRepository.state.collectAsState(),
                    satellites = locationRepository.satellites.collectAsState()
                ),
                //TODO this needs a KnobListener state, and to have the poi overlay be built outside of here.
                poiOverlay = (poiList + parameters.markers).let { list ->
                    logger.d("POI LIST", list.toString())
                    PoiOverlay(
                        pois = list.map { poi ->
                            PoiOverlay.PoiOverlayItem(
                                label = poi.name,
                                position = poi.location,
                                icon = { Box{
                                    when (poi.icon) {
                                        is PoiRepository.Poi.PoiIcon.ColoredCircle -> PoiOverlay.PoiOverlayItem.CIRCLE_COLOR.invoke(poi.icon.color)
                                        is PoiRepository.Poi.PoiIcon.BundledIcon -> PoiOverlay.PoiOverlayItem.ICON_FILE.invoke(poi.icon.drawableResource, poi.icon.tint)
                                        else -> {}
                                    }
                                }
                                }
                            )
                        }.let { list : List<PoiOverlay.PoiOverlayItem> ->
                            val marker = gpsMarker.value
                            if (marker != null) {
                                list + listOf(marker)
                            } else {
                                list
                            }
                        }.let { list ->
                            list + selectablePois
                        }
                    )
                },
                route = routeLatLng?.let { path ->
                    Route(
                        path = path,
                        color = Color.Magenta,
                        stroke = Stroke(8F)
                    )
                }
            ),
            extents = extents,
            onCenterPositionUpdated = { newCenter ->
                extents.centerState = newCenter
                // Update the map center provider so it knows where to pan from
                // if gps tracking is enabled.
                gpsMapCenterProvider.onUserMapCenterChanged(extents.centerState, extents.mapScaleState )
            },
            onViewportSizeChanged = { widthDp, heightDp ->
                viewPortWidth = widthDp.dp
                viewPortHeight = heightDp.dp
                gpsMapCenterProvider.onMapViewportChanged(widthDp, heightDp)
            }
        )

    }

    private fun showModifyViewMenu(
        mapScreenOpenMode: MapScreenParameters.MapScreenOpenMode,
        currentCenterAcessor : () -> LatLng,
        currentPoiAccessor : () -> MapPoi?,
        onOverlayStateChanged : (new : MapOverlayState) -> Unit,
        isLocationServiceRunningAccessor : () -> Boolean,
        onRequestRecenterToLocation : () -> Unit,
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
                    ).let {
                        if (isLocationServiceRunningAccessor()) {
                            it + listOf(
                                ModalMenu.ModalMenuItem(
                                    title = "Recenter on Gps",
                                    onClicked = {
                                        onRequestRecenterToLocation()
                                    }
                                )
                            )
                        } else {
                            it
                        }
                    }

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
                        is MapScreenParameters.MapScreenOpenMode.PoiBrowser -> {
                            val poiMode = mapScreenOpenMode.poiMode

                            val currentPoi = currentPoiAccessor()

                            val selectionEntry = if (currentPoi != null) {
                                listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = "View POI details",
                                        onClicked = {
                                            modalMenuService.closeModalMenu()
                                            showSelectablePoiSideMenu(currentPoi, mapScreenOpenMode) {
                                                onOverlayStateChanged(it)
                                            }
                                        }
                                    )
                                )
                            } else {
                                listOf()
                            }

                            val closeEntries = when (poiMode) {
                                MapScreenParameters.MapScreenOpenMode.PoiBrowser.PoiMode.Select -> {
                                    //You have to select a point to get a point selected result.
                                    listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "Exit Map without selection",
                                            onClicked = {
                                                modalMenuService.closeModalMenu()
                                                navigationNodeTraverser.setResultAndGoBack(
                                                    this,
                                                    MapScreenResult.PointSelectedResult(null)
                                                )
                                            }
                                        ),
                                    )
                                }
                                MapScreenParameters.MapScreenOpenMode.PoiBrowser.PoiMode.Browse,
                                MapScreenParameters.MapScreenOpenMode.PoiBrowser.PoiMode.BrowseOfferNavigation-> {
                                    listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "Main Menu",
                                            onClicked = {
                                                navigationNodeTraverser.navigateToRoot()
                                            }
                                        )
                                    )
                                }
                            }
                            selectionEntry + closeEntries
                        }
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
                            onOverlayStateChanged(MapOverlayState.NoOverlay)
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
                        title = "Places Nearby...",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            PlacesNearbyScreen.open(navigationNodeTraverser, currentCenter)
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


    private fun showSelectablePoiSideMenu(
        poi : MapPoi,
        openMode : MapScreenParameters.MapScreenOpenMode.PoiBrowser,
        onOverlayStateChanged : (new : MapOverlayState) -> Unit,
    ) {

        //Show all the map POI details
        // based on the open mode, can select or exit the screen.

        //TODO When this closes, it needs to set the browse mode so that scroll still works.

        logger.d(TAG, "showSelectablePoiSideMenu for poi $poi")

        modalMenuService.showSidePaneOverlay(darkenBackground = false) {
            SidePanelMenu.SidePanelMenu(
                title = MapPoiLabels.name(poi),
                text = @Composable {
                    MapPoiLabels.detailLines(poi).forEach { InfoLabel(it) }
                },
                buttons = buildList {
                    if (openMode.poiMode == MapScreenParameters.MapScreenOpenMode.PoiBrowser.PoiMode.Select) {
                        add(
                            TextMenuItem(
                                title = "Select this POI",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay()
                                    navigationNodeTraverser.setResultAndGoBack(
                                        MapScreen::class.java,
                                        MapScreenResult.PointSelectedResult(
                                            poi.location
                                        )
                                    )
                                    onOverlayStateChanged.invoke(MapOverlayState.NoOverlay)
                                }
                            )
                        )
                    }

                    if (openMode.poiMode == MapScreenParameters.MapScreenOpenMode.PoiBrowser.PoiMode.BrowseOfferNavigation) {
                        add(
                            TextMenuItem(
                                title = "Set as Guidance start",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay()
                                    backgroundGuidanceSetupTool.setPoiAsStart(poi)
                                    onOverlayStateChanged.invoke(MapOverlayState.NoOverlay)
                                }
                            )
                        )
                        add(
                            TextMenuItem(
                                title = "Set as Guidance end",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay()
                                    backgroundGuidanceSetupTool.setPoiAsEnd(poi)
                                    onOverlayStateChanged.invoke(MapOverlayState.NoOverlay)
                                }
                            )
                        )
                    }

                    //TODO maybe someday, buttons to dial the phone number?

                    add(TextMenuItem(
                        title = "Close Side-pane",
                        onClicked = {
                            onOverlayStateChanged.invoke(MapOverlayState.NoOverlay)
                            modalMenuService.closeSidePaneOverlay()
                        }
                    ))
                }
            )
        }

    }


}