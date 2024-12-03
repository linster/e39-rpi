package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.overlayMenus

import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.guidance.setupScreens.GuidanceSetupScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import javax.inject.Inject

class GuidanceMenu @Inject constructor(
    private val overlayMenuNavigator: OverlayMenuNavigator,
    private val modalMenuService: ModalMenuService,
    private val guidanceService: GuidanceService
): OverlayMenu {

    override fun visibleOnStates() = listOf(MapOverlayState.GuidanceMenu)


    fun showMenu() {
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512,
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
                        title = let{
                            if (guidanceService.getInstantaneousGuidanceSessionState() != GuidanceSession.SessionState.IN_GUIDANCE) {
                                "Setup..."
                            } else {
                                "Direction Options"
                            }
                        },
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            overlayMenuNavigator.navigateToScreen(GuidanceSetupScreen::class.java)
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Terminate Guidance",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            guidanceService.stopGuidance()
                            overlayMenuNavigator.requestChangeOverlayState(MapOverlayState.NoOverlay)
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

}