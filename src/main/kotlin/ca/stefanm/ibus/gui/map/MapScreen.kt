package ca.stefanm.ibus.gui.map

import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject

@AutoDiscover
class MapScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val logger : Logger
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "MapScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapScreen::class.java


    sealed class MapOverlayState(val nextStateOnClick : MapOverlayState?, val previousStateOnBack : MapOverlayState?) {
        object NoOverlay : MapOverlayState(nextStateOnClick = ModifyViewMenu, previousStateOnBack = null)
        object ModifyViewMenu : MapOverlayState(nextStateOnClick = null, previousStateOnBack = NoOverlay)
        object GuidanceMenu : MapOverlayState(nextStateOnClick = null, previousStateOnBack = ModifyViewMenu)
        object PanLeftRight : MapOverlayState(nextStateOnClick = ModifyViewMenu, previousStateOnBack = null)
        object PanUpDown : MapOverlayState(nextStateOnClick = ModifyViewMenu, previousStateOnBack = null)
        object ChangeZoom : MapOverlayState(nextStateOnClick = ModifyViewMenu, previousStateOnBack = null)
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val extents = remember { mutableStateOf(Extents(
            center = GeoPosition(45.3481002,-76.0053771),
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

        LaunchedEffect(currentOverlayState.value) {
            logger.d(TAG, "Overlay State: ${currentOverlayState.value}")
            if (currentOverlayState.value == MapOverlayState.ModifyViewMenu) {
                showModifyViewMenu(currentOverlayStateBus)
            }
            if (currentOverlayState.value == MapOverlayState.GuidanceMenu) {
                showGuidanceMenu(currentOverlayStateBus)
            }
        }



        MapViewer(
            overlayProperties = OverlayProperties(
                mapScaleVisible = currentOverlayState.value == MapOverlayState.ChangeZoom,
                centerCrossHairsVisible = currentOverlayState.value == MapOverlayState.PanUpDown || currentOverlayState.value == MapOverlayState.PanLeftRight,
                gpsReceptionIconVisible = false
            ),
            extents = extents.value,
            onCenterPositionUpdated = {
                currentCenter.value = it
            }
        )

    }

    private fun showModifyViewMenu(currentOverlayStateBus : MutableStateFlow<MapOverlayState>) {
        modalMenuService.showModalMenu(
            menuTopLeft = IntOffset(32, 32),
            menuWidth = 384,
            autoCloseOnSelect = true,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu.previousStateOnBack!!
                },
                items = listOf(
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
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Guidance",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            currentOverlayStateBus.value = MapOverlayState.GuidanceMenu
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Main Menu",
                        onClicked = {
                            navigationNodeTraverser.navigateToRoot()
                        }
                    )
                )
            )
        )
    }
    
    private fun showGuidanceMenu(currentOverlayStateBus : MutableStateFlow<MapOverlayState>) {
        modalMenuService.showModalMenu(
            menuTopLeft = IntOffset(32, 32),
            menuWidth = 512,
            autoCloseOnSelect = true,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    currentOverlayStateBus.value = MapOverlayState.ModifyViewMenu.previousStateOnBack!!
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




}