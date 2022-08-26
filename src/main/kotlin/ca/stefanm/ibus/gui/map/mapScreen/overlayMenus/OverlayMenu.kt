package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.overlayMenus

import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapOverlayState
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import com.javadocmd.simplelatlng.LatLng

interface OverlayMenu {

    fun visibleOnStates() : List<MapOverlayState>


}

interface OverlayMenuNavigator {
    fun requestChangeOverlayState(new : MapOverlayState)

    fun navigateToScreen(node : Class<out NavigationNode<*>>)

    fun customNavigateToScreen(block : (navigationNodeTraverser: NavigationNodeTraverser) -> Unit)
}

interface OverlayMenuStateAccessor {
    fun getCurrentMapCenter() : LatLng

    fun getMapOpenMode() : MapScreenParameters.MapScreenOpenMode
}
