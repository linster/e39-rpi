package ca.stefanm.ibus.gui.map.poi

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.stefanm.ibus.gui.docs.GuidanceScreenDocPartition
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.ui.CurrentDeviceChooser
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import javax.inject.Inject

@ScreenDoc(
    screenName = "PoiSelectorScreen",
    description = "Allows the user to select a point on a map from the POI addressbook."
)
@ScreenDoc.AllowsGoBack([
    ScreenDoc.AllowsGoBack.Description("Location Selected"),
    ScreenDoc.AllowsGoBack.Description("No location selected")
])
@GuidanceScreenDocPartition
@AutoDiscover
class PoiSelectorScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val poiRepository: PoiRepository
) : NavigationNode<PoiSelectorScreen.PoiSelectionResult>{

    sealed interface PoiSelectionResult {
        object NothingSelected : PoiSelectionResult
        data class PoiSelected(val poi : PoiRepository.Poi) : PoiSelectionResult
    }

    override val thisClass: Class<out NavigationNode<PoiSelectionResult>> = PoiSelectorScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {
            BmwSingleLineHeader("Select POI from Address Book")

            ScrollMenu.OneColumnScroll(
                onScrollListExitSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this@PoiSelectorScreen,
                        PoiSelectionResult.NothingSelected
                    )
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 4,
                    isExitItemOnEveryPage = false,
                    isPageCountItemVisible = true,
                    showSpacerRow = false,
                    exitListItemLabel = "Cancel Selection",
                ),
                items = poiRepository.getAllPois().map {
                    //The user can't chang the POI list while making a selection, so it's
                    //okay the UI won't live-update.

                    TextMenuItem(
                        title = it.name,
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(
                                this@PoiSelectorScreen,
                                PoiSelectionResult.PoiSelected(it)
                            )
                        }
                    )
                }
            )
        }
    }
}