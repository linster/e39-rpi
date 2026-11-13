package ca.stefanm.ibus.gui.map.places

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenParameters
import ca.stefanm.ibus.gui.map.repo.RemoteMapPoiRepository
import ca.stefanm.ibus.gui.map.repo.RemoteMapPoiRepository.SearchState
import ca.stefanm.ibus.gui.map.repo.RemotePoiRepository
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Places from the map data matching a [PlacesQuery], nearest first, with
 * their distance and direction from its centre. A place opens a side pane:
 * what OpenStreetMap knows about it, show it on the map, or save it in the
 * address book (the user's POIs).
 */
@ScreenDoc(
    screenName = "PlacesListScreen",
    description = "Places from the map data near a point, nearest first; details, show on map, save to the address book.",
    navigatesTo = [
        ScreenDoc.NavigateTo(MapScreen::class)
    ]
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.OpensSubScreen("placePane")
@AutoDiscover
class PlacesListScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val mapPoiRepository: RemoteMapPoiRepository,
    private val poiRepository: RemotePoiRepository,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "PlacesListScreen"

        fun open(navigationNodeTraverser: NavigationNodeTraverser, query: PlacesQuery) {
            navigationNodeTraverser.navigateToNodeWithParameters(PlacesListScreen::class.java, query)
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PlacesListScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val query = params?.requestParameters as? PlacesQuery
        if (query == null) {
            // Only reachable through open(); go back rather than guess a place.
            LaunchedEffect(Unit) { navigationNodeTraverser.goBack() }
        } else {
            PlacesList(query)
        }
    }

    @Composable
    private fun PlacesList(query: PlacesQuery) {
        // Remembered: each collection searches the backend.
        val state = remember(query) { mapPoiRepository.search(query) }
            .collectAsState(SearchState.Searching).value
        val scope = rememberCoroutineScope()

        val items: List<MenuItem> = buildList {
            fun status(text: String) = add(TextMenuItem(text, isSelectable = false, onClicked = {}))
            when (state) {
                SearchState.Searching -> status("Searching...")
                is SearchState.Failed ->
                    status(if (state.retrying) "Maps backend not reachable; retrying..." else "Can't search: ${state.reason}")
                is SearchState.Found -> {
                    if (state.places.isEmpty()) {
                        status("Nothing within ${MapPoiLabels.distance(state.radiusMeters)}")
                    }
                    state.places.forEach { found ->
                        add(TextMenuItem(
                            title = MapPoiLabels.listEntry(found, query.center),
                            onClicked = { showPlacePane(scope, query, found, state.places) },
                        ))
                    }
                }
            }
        }

        // SmoothScroll allocates knob positions once: rebuild it when the
        // results arrive.
        key(items.size, state) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "${query.title} near ${query.centerLabel}",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    @ScreenDoc.SubScreen(
        screenName = "placePane",
        paneDescription = "What the map data knows about a place (category, address, phone, hours, ...), " +
                "with Show on map and Save as POI (to the address book)."
    )
    private fun showPlacePane(scope: CoroutineScope, query: PlacesQuery, found: FoundMapPoi, all: List<FoundMapPoi>) {
        val poi = found.poi
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = MapPoiLabels.name(poi),
                text = @Composable {
                    InfoLabel(
                        "${MapPoiLabels.distanceAndDirection(found, query.center)} from ${query.centerLabel}",
                        FontWeight.Bold
                    )
                    MapPoiLabels.detailLines(poi).forEach { InfoLabel(it) }
                },
                buttons = listOf(
                    TextMenuItem("Show on map", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        showOnMap(found, all)
                    }),
                    TextMenuItem("Save as POI", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        saveToAddressBook(scope, poi)
                    }),
                    TextMenuItem("Go back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    }),
                )
            )
        }
    }

    /** The map centred on the place, with the other results around it. */
    private fun showOnMap(found: FoundMapPoi, all: List<FoundMapPoi>) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            MapScreen::class.java,
            MapScreenParameters(
                persistUiStateOnClose = false,
                // Not the last browsing position: this place.
                usePersistedStateOnOpen = false,
                openMode = MapScreenParameters.MapScreenOpenMode.BrowsingMode(center = found.poi.location),
                markers = all.map { it.poi.toUserPoi() },
            )
        )
    }

    private fun saveToAddressBook(scope: CoroutineScope, poi: MapPoi) {
        scope.launch {
            poiRepository.create(poi.toUserPoi().copy(name = MapPoiLabels.name(poi))).fold(
                onSuccess = {
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.MAP_WAYPOINT, "Saved as POI", it.name)
                    )
                },
                onFailure = { e ->
                    logger.w(TAG, "Not saved: ${e.message}")
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Not saved", e.message ?: e.toString())
                    )
                }
            )
        }
    }
}
