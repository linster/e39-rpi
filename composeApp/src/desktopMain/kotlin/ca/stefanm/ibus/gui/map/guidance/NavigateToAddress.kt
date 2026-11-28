package ca.stefanm.ibus.gui.map.guidance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.map.guidance.GuidanceService.RoutePoint
import ca.stefanm.ibus.gui.map.guidance.setupScreens.GuidanceSetupScreen
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Candidate
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Precision
import ca.stefanm.ibus.gui.map.repo.MapsBackendException
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "Navigate" to an address (#59): looks it up on the map ([GeocodeRepository],
 * near the car when there's a position), lets the user pick a match (shown on
 * a small map) and whether it's the route's start or end, then opens Guidance
 * Setup with that point set. A calculated route or running guidance is only
 * replaced after asking.
 *
 * Used by contact details; a calendar event's location can use it too.
 */
class NavigateToAddress @Inject constructor(
    private val geocodeRepository: GeocodeRepository,
    private val locationRepository: LocationRepository,
    private val guidanceService: GuidanceService,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "NavigateToAddress"

        /** How exact a match is, for the user. */
        fun Precision.describe(): String = when (this) {
            Precision.ADDRESS -> "address"
            Precision.INTERPOLATED -> "address (estimated)"
            Precision.STREET -> "street only"
            Precision.PLACE -> "town only"
            Precision.POSTCODE -> "postal code area"
            Precision.UNKNOWN -> "approximate"
        }
    }

    /** Looks up [address] ([shownAs]: one line, for the panes) in [scope], then shows the matches. */
    fun navigate(scope: CoroutineScope, address: GeocodeRepository.Address, shownAs: String) {
        scope.launch {
            val near = locationRepository.getPositionStateFlow().value
            geocodeRepository.geocode(address = address, near = near).fold(
                onSuccess = { candidates ->
                    logger.d(TAG, "$shownAs: ${candidates.size} candidates")
                    if (candidates.isEmpty()) {
                        notify("Address not found on the map", shownAs)
                    } else {
                        showCandidates(shownAs, candidates)
                    }
                },
                onFailure = { e ->
                    logger.w(TAG, "Geocoding $shownAs: $e")
                    when (e) {
                        is GeocodeRepository.NoGeocodingData ->
                            notify("Can't look up addresses", "This map has no address data: update it")
                        is MapsBackendException.Unavailable ->
                            notify("Can't reach the maps backend", e.message ?: "")
                        else -> notify("Couldn't look up the address", e.message ?: e.toString())
                    }
                },
            )
        }
    }

    private fun showCandidates(shownAs: String, candidates: List<Candidate>) {
        modalMenuService.showMailSidePane(
            logger, "$TAG candidates", "Navigate to",
            info = listOf(shownAs, if (candidates.size == 1) "1 match" else "${candidates.size} matches"),
            items = candidates.map { c ->
                SidePaneItem("${c.label} (${c.precision.describe()})") { showCandidate(shownAs, candidates, c) }
            },
        )
    }

    /** The match on a small map, and whether it's the start or the end. */
    private fun showCandidate(shownAs: String, candidates: List<Candidate>, c: Candidate) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.LatLngDetailSidePanelMenu(
                title = c.label,
                poi = PoiRepository.Poi(c.label, c.location, PoiRepository.Poi.PoiIcon.ColoredCircle(Color.Red), isVisible = true),
                centerCrossHairsVisible = false,
                mapScale = if (c.precision.isExact || c.precision == Precision.STREET) MapScale.METERS_400 else MapScale.KILOMETERS_1_6,
                buttons = listOf(
                    TextMenuItem("Go Back", onClicked = { showCandidates(shownAs, candidates) }),
                    TextMenuItem("Use as Start", onClicked = { use(RoutePoint.START, c) }),
                    TextMenuItem("Use as End", onClicked = { use(RoutePoint.END, c) }),
                ),
            )
        }
    }

    private fun use(which: RoutePoint, c: Candidate) {
        val state = guidanceService.getInstantaneousGuidanceSessionState()
        if (state == GuidanceSession.SessionState.SETTING_UP || state == GuidanceSession.SessionState.READY_TO_CALCULATE) {
            set(which, c)
            return
        }
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Replace the current route?",
                text = @Composable {
                    if (state == GuidanceSession.SessionState.IN_GUIDANCE) InfoLabel("Guidance will end.")
                    InfoLabel("${if (which == RoutePoint.START) "Start" else "End"}: ${c.label}")
                },
                buttons = listOf(
                    TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Replace", onClicked = { set(which, c) }),
                ),
            )
        }
    }

    private fun set(which: RoutePoint, c: Candidate) {
        modalMenuService.closeSidePaneOverlay(true)
        logger.i(TAG, "$which = ${c.label} ${c.location}")
        guidanceService.replacePoint(which, c.location)
        navigationNodeTraverser.navigateToNode(GuidanceSetupScreen::class.java)
    }

    private fun notify(top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(Notification.NotificationImage.MAP_GENERAL, top, content))
    }
}
