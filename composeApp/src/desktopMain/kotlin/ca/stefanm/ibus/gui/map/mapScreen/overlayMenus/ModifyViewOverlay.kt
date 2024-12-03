package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.overlayMenus

import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import javax.inject.Inject

class ModifyViewOverlay @Inject constructor(
    private val overlayMenuNavigator: OverlayMenuNavigator,
    private val overlayMenuStateAccessor: OverlayMenuStateAccessor,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
) : OverlayMenu {

    override fun visibleOnStates() = listOf(MapOverlayState.ModifyViewMenu)

    fun showMenu() {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 384,
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = true,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.NoOverlay)
                },
                items = let {
                    val zoomControls = listOf(
                        ModalMenu.ModalMenuItem(
                            title = "Zoom",
                            onClicked = {
                                overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.ChangeZoom)
                            }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬌",
                            onClicked = { overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.PanLeftRight) }
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Pan ⬍",
                            onClicked = { overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.PanUpDown) }
                        )
                    )

                    val browsingEntries = if (overlayMenuStateAccessor.getMapOpenMode() is MapScreenParameters.MapScreenOpenMode.BrowsingMode) {
                        listOf(
                            ModalMenu.ModalMenuItem(
                                title = "Guidance",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.GuidanceMenu)
                                }
                            ),
                            ModalMenu.ModalMenuItem(
                                title = "POIs",
                                onClicked = {
                                    modalMenuService.closeModalMenu()
                                    overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.PoiMenu)
                                }
                            )
                        )
                    } else { listOf() }

                    val closingEntries = when (overlayMenuStateAccessor.getMapOpenMode()) {
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
                                            MapScreen::class.java,
                                            MapScreenResult.PointSelectedResult(null)
                                        )
                                    }
                                ),
                                ModalMenu.ModalMenuItem(
                                    title = "Select Center",
                                    onClicked = {
                                        modalMenuService.closeModalMenu()
                                        navigationNodeTraverser.setResultAndGoBack(
                                            MapScreen::class.java,
                                            MapScreenResult.PointSelectedResult(
                                                overlayMenuStateAccessor.getCurrentMapCenter()
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

}