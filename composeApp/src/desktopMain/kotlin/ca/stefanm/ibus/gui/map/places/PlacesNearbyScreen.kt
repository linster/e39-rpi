package ca.stefanm.ibus.gui.map.places

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject

/**
 * Places from the map data near a point: pick a category (or search by
 * name), then [PlacesListScreen] lists them nearest first.
 */
@ScreenDoc(
    screenName = "PlacesNearbyScreen",
    description = "Pick a category of places from the map data (fuel, restaurants, ...) or search them by name, " +
            "near the map's centre.",
    navigatesTo = [
        ScreenDoc.NavigateTo(PlacesListScreen::class)
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PlacesNearbyScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val configurationStorage: ConfigurationStorage,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "PlacesNearbyScreen"

        /** Places near [center]; [centerLabel] names it in headers ("map centre"). */
        fun open(navigationNodeTraverser: NavigationNodeTraverser, center: LatLng, centerLabel: String = "map centre") {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PlacesNearbyScreen::class.java,
                PlacesQuery(center = center, centerLabel = centerLabel)
            )
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PlacesNearbyScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        // Without parameters (e.g. from a menu): near the default map centre.
        val near = params?.requestParameters as? PlacesQuery
            ?: configurationStorage.config[E39Config.MapConfig.defaultMapCenter].let {
                PlacesQuery(center = LatLng(it.first, it.second), centerLabel = "home")
            }

        val items: List<MenuItem> = buildList {
            add(TextMenuItem("Search by name...", onClicked = { searchByName(near) }))
            add(TextMenuItem("All places", onClicked = { PlacesListScreen.open(navigationNodeTraverser, near) }))
            MapPoiCategory.entries.forEach { category ->
                add(TextMenuItem(category.title, onClicked = {
                    PlacesListScreen.open(navigationNodeTraverser, near.copy(categories = setOf(category)))
                }))
            }
        }

        SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
            header = "Places near ${near.centerLabel}",
            prependGoBackEntry = true,
            items = items,
        )
    }

    private fun searchByName(near: PlacesQuery) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { entered ->
            val name = entered.trim()
            if (name.isEmpty()) return@showKeyboard
            PlacesListScreen.open(navigationNodeTraverser, near.copy(nameContains = name))
        }
    }
}
