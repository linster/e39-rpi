package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.overlayMenus.*
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject

@ApplicationScope
class OverlayMenuManager @Inject constructor(
    private val overlayMenuStateAccessor: OverlayMenuStateAccessor,
    private val guidanceMenu: GuidanceMenu,
    private val modifyViewOverlay: ModifyViewOverlay,
    private val poiOverlayMenu: PoiOverlayMenu
) : OverlayMenuNavigator {
    override fun requestChangeOverlayState(new: MapOverlayState) {
        TODO("Not yet implemented")
    }

    override fun navigateToScreen(node: Class<out NavigationNode<*>>) {
        TODO("Not yet implemented")
    }

    override fun customNavigateToScreen(block: (navigationNodeTraverser: NavigationNodeTraverser) -> Unit) {
        TODO("Not yet implemented")
    }

}