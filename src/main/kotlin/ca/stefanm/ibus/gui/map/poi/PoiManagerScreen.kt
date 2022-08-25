package ca.stefanm.ibus.gui.map.poi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.*
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.StdOutLogger
import kotlinx.coroutines.flow.filter
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject

@AutoDiscover
class PoiManagerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val poiRepository: PoiRepository
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PoiManagerScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("POI Address book")


            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    ),
                    TextMenuItem(
                        title = "Create New Poi",
                        onClicked = {
                            CreateOrEditPoiScreen.openForPoiCreation(navigationNodeTraverser)
                        }
                    )
                ) + poiRepository.getAllPois().map {
                    TextMenuItem(
                        title = it.name,
                        onClicked = {
                            viewPoi(it)
                        }
                    )
                }
            )
        }
    }

    private fun viewPoi(poi: PoiRepository.Poi) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {

            val isVisible : State<Boolean>

            SidePanelMenu.LatLngDetailSidePanelMenu(
                title = "POI: ${poi.name}",
                poi = poi,
                centerCrossHairsVisible = true,
                mapScale = MapScale.METERS_400,
                buttons = listOf(
//                    CheckBoxMenuItem(
//                        title = "Visible on Map?",
//                        isChecked = remember { poiRepository.getVisibilityForPoi(poi) }.collectAsState(false).value,
//                        onClicked = {
//                            poiRepository.toggleVisibilityForPoi(poi)
//                        }
//                    ),
                    TextMenuItem(
                        title = "Open map at...",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            MapScreen.openForBrowsingAtLocation(navigationNodeTraverser, poi.location, clearBackStack = true)
                        }
                    ),
                    //TODO add a "Navigate to.." option here.
                    TextMenuItem(
                        title = "Edit / Delete",
                        onClicked = {
                            modalMenuService.showModalMenu(
                                dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                                    menuWidth = 512,
                                    menuTopLeft = IntOffset(1200, 300)
                                ).toNormalModalMenuDimensions(),
                                autoCloseOnSelect = false,
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.E,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "Edit",
                                            onClicked = {
                                                modalMenuService.closeModalMenu()
                                                modalMenuService.closeSidePaneOverlay(true)
                                                CreateOrEditPoiScreen.editExistingPoi(navigationNodeTraverser, poi)
                                            }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "Delete",
                                            onClicked = {
                                                modalMenuService.closeModalMenu()
                                                modalMenuService.closeSidePaneOverlay(true)
                                                poiRepository.deletePoi(poi)
                                            }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "Go Back",
                                            onClicked = {
                                                modalMenuService.closeModalMenu()
                                            }
                                        )
                                    )
                                )
                            )

                        }
                    ),
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    ),
                )
            )
        }
    }
}