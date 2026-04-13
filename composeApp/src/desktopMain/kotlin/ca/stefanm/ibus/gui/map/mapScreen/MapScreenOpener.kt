package ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import com.javadocmd.simplelatlng.LatLng

interface MapScreenOpener {
    //Open the map screen in such a way so that the user can select a point on the map.
    //When the point is selected, return a result
    fun openForUserLocationSelection(navigationNodeTraverser: NavigationNodeTraverser) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            MapScreen::class.java,
            MapScreenParameters(
                persistUiStateOnClose = false,
                openMode = MapScreenParameters.MapScreenOpenMode.LocationSelection(
                    center = LatLng(45.3154699,-75.9194058)
                )
            )
        )
    }

    fun openForUserLocationSelection(
        navigationNodeTraverser: NavigationNodeTraverser,
        centerOn : LatLng
    ) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            MapScreen::class.java,
            MapScreenParameters(
                persistUiStateOnClose = false,
                openMode = MapScreenParameters.MapScreenOpenMode.LocationSelection(
                    center = centerOn
                )
            )
        )
    }

    fun openForBrowsingAtLocation(
        navigationNodeTraverser: NavigationNodeTraverser,
        centerOn: LatLng,
        clearBackStack : Boolean = false
    ) {
        if (clearBackStack) {
            navigationNodeTraverser.navigateToRoot()
        }
        navigationNodeTraverser.navigateToNodeWithParameters(
            MapScreen::class.java,
            MapScreenParameters(
                persistUiStateOnClose = false,
                usePersistedStateOnOpen = true,
                openMode = MapScreenParameters.MapScreenOpenMode.BrowsingMode(
                    center = centerOn
                )
            )
        )
    }
}