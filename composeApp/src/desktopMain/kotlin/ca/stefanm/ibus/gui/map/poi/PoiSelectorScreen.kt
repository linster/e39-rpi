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
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
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

    companion object {
        const val TAG = "PoiSelectorScreen"
    }
    sealed interface PoiSelectionResult {
        object NothingSelected : PoiSelectionResult
        data class PoiSelected(val poi : PoiRepository.Poi) : PoiSelectionResult
    }

    override val thisClass: Class<out NavigationNode<PoiSelectionResult>> = PoiSelectorScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val context = SmoothScroll.MenuWindowSmoothScrollContext()

        //TODO this also needs a search item, that opens a pane, and that filters the list by name
        context.OneColumnSmoothScreen(
            header = "Select POI from Address Book",
            logTag = TAG,
            prependGoBackEntry = false,
            items = listOf(
                TextMenuItem(
                    "Cancel Selection",
                    onClicked = {
                        navigationNodeTraverser.setResultAndGoBack(this@PoiSelectorScreen,
                            PoiSelectionResult.NothingSelected
                        )
                    }
                )) + poiRepository.getAllPois().map {
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