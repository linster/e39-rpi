package ca.stefanm.ibus.gui.map.mapScreen.overlayMenus

import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.poi.CreateOrEditPoiScreen
import ca.stefanm.ibus.gui.map.poi.PoiManagerScreen
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import javax.inject.Inject

class PoiOverlayMenu @Inject constructor(
    private val overlayMenuNavigator: OverlayMenuNavigator,
    private val overlayMenuStateAccessor: OverlayMenuStateAccessor,
    private val modalMenuService: ModalMenuService,
    private val poiRepository: PoiRepository,
) : OverlayMenu {

    override fun visibleOnStates() = listOf(MapOverlayState.PoiMenu)

    fun showMenu() {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = false,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                onClose = {
                    overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.ModifyViewMenu)
                },
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Back",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.ModifyViewMenu)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Open Address Book...",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.navigateToScreen(PoiManagerScreen::class.java)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "New POI on Center",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.customNavigateToScreen { navigationNodeTraverser ->
                                CreateOrEditPoiScreen.newPoiAtLocation(
                                    navigationNodeTraverser,
                                    overlayMenuStateAccessor.getCurrentMapCenter()
                                )
                            }
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Hide All POIs",
                        onClicked = {
                            poiRepository.hideAllPois()
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.ModifyViewMenu)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Show All Stored POIs",
                        onClicked = {
                            poiRepository.showAllPois()
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.ModifyViewMenu)
                        }
                    )
                )
            )
        )
    }

}