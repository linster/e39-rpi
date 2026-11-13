package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.system.TimeZoneSettings
import ca.stefanm.ibus.lib.system.ZoneEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Settings -> Time Zone: automatic (the installed map's zone), or a zone
 * picked from Canada's, the United States' or any region's (tzdata's
 * zone1970.tab). The system zone changes (TimeZoneSettings), not just the
 * HMI's.
 */
@ScreenDoc(
    screenName = "TimeZoneScreen",
    description = "Set the time zone: automatic (from the map), or pick one by country or region.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class TimeZoneScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val timeZoneSettings: TimeZoneSettings,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "TimeZoneScreen"
    }

    /** What the screen lists: the top level, one country's zones, or one region's. */
    sealed interface Level {
        data object Top : Level
        data class Country(val code: String, val name: String) : Level
        data class Region(val region: String) : Level
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = TimeZoneScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val level = params?.requestParameters as? Level ?: Level.Top
        val scope = rememberCoroutineScope()
        // What's set; read again for each visit (the three levels are this
        // same screen, so a plain remember would keep the first visit's) and
        // after a change, so the checkmarks follow.
        val current = remember(params) { mutableStateOf(timeZoneSettings.current()) }
        val mode = remember(params) { mutableStateOf(timeZoneSettings.mode()) }
        val zones = remember { timeZoneSettings.zones() }

        fun refresh() {
            current.value = timeZoneSettings.current()
            mode.value = timeZoneSettings.mode()
        }

        val items: List<MenuItem> = when (level) {
            Level.Top -> topItems(scope, current.value, mode.value, zones.isNotEmpty(), ::refresh)
            is Level.Country -> zoneItems(scope, TimeZoneLabels.countryZones(zones, level.code), current.value,
                mode.value, byComment = true, ::refresh)
            is Level.Region -> zoneItems(scope, TimeZoneLabels.regionZones(zones, level.region), current.value,
                mode.value, byComment = false, ::refresh)
        }

        val header = when (level) {
            Level.Top -> "Time Zone"
            is Level.Country -> "Time Zone: ${level.name}"
            is Level.Region -> "Time Zone: ${level.region}"
        }

        key(current.value, mode.value, items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext("$TAG-$level").OneColumnSmoothScreen(
                header = header,
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun topItems(
        scope: CoroutineScope,
        current: ZoneId,
        mode: TimeZoneSettings.Mode,
        haveZones: Boolean,
        refresh: () -> Unit,
    ): List<MenuItem> = buildList {
        add(TextMenuItem("Now: ${TimeZoneLabels.describe(current)}", isSelectable = false, onClicked = {}))
        if (!timeZoneSettings.canChange()) {
            add(TextMenuItem("Can't be changed on this system", isSelectable = false, onClicked = {}))
            return@buildList
        }
        add(TextMenuItem(TimeZoneLabels.mark(mode == TimeZoneSettings.Mode.AUTOMATIC, "Automatic (from the map)"), onClicked = {
            apply(scope, "Automatic time zone", refresh) { timeZoneSettings.automatic() }
        }))
        if (haveZones) {
            add(TextMenuItem("Canada", onClicked = { open(Level.Country("CA", "Canada")) }))
            add(TextMenuItem("United States", onClicked = { open(Level.Country("US", "United States")) }))
            TimeZoneLabels.REGIONS.forEach { region ->
                add(TextMenuItem(region, onClicked = { open(Level.Region(region)) }))
            }
        }
    }

    private fun zoneItems(
        scope: CoroutineScope,
        zones: List<ZoneEntry>,
        current: ZoneId,
        mode: TimeZoneSettings.Mode,
        byComment: Boolean,
        refresh: () -> Unit,
    ): List<MenuItem> = zones.map { zone ->
        val chosen = mode == TimeZoneSettings.Mode.MANUAL && zone.id == current.id
        TextMenuItem(TimeZoneLabels.mark(chosen, TimeZoneLabels.label(zone, byComment)), onClicked = {
            // Chosen: back to the top level, which shows it.
            apply(scope, "Time zone", refresh, thenBack = true) { timeZoneSettings.set(zone.id) }
        })
    }

    private fun open(level: Level) {
        navigationNodeTraverser.navigateToNodeWithParameters(TimeZoneScreen::class.java, level)
    }

    private fun apply(
        scope: CoroutineScope,
        what: String,
        refresh: () -> Unit,
        thenBack: Boolean = false,
        change: suspend () -> Result<ZoneId>,
    ) {
        scope.launch {
            change().fold(
                onSuccess = {
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.MESSAGE_CIRCLE, what, TimeZoneLabels.describe(it))
                    )
                    if (thenBack) {
                        navigationNodeTraverser.goBack()
                        return@launch
                    }
                },
                onFailure = {
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Time zone not changed", it.message ?: it.toString())
                    )
                }
            )
            refresh()
        }
    }
}

/** Text for the time zone screen (no Compose: unit-tested). */
object TimeZoneLabels {

    /** tzdata's regions (the first part of zone ids), for "pick by region". */
    val REGIONS = listOf("Africa", "America", "Antarctica", "Asia", "Atlantic", "Australia", "Europe", "Indian", "Pacific")

    fun mark(on: Boolean, text: String) = (if (on) "\u2713 " else "   ") + text

    /** A country's zones in tzdata's order (roughly east to west for Canada). */
    fun countryZones(zones: List<ZoneEntry>, code: String) = zones.filter { code in it.countryCodes }

    /** A region's zones, by city. */
    fun regionZones(zones: List<ZoneEntry>, region: String) =
        zones.filter { it.region == region }.sortedBy { it.city }

    /**
     * In a country's list: "Eastern - ON & QC (most areas) (Toronto)", as the
     * comment says where; in a region's list: "Toronto (Eastern - ON & QC
     * (most areas))". Zones without a comment: the city.
     */
    fun label(zone: ZoneEntry, byComment: Boolean): String = when {
        zone.comment.isBlank() -> zone.city
        byComment -> "${zone.comment} (${zone.city})"
        else -> "${zone.city} (${zone.comment})"
    }

    /**
     * "America/Toronto, 4:26 PM, UTC-5". The offset, not "EST": Java's zone
     * names come from CLDR, not from tzdata's rules, and lag behind them
     * (Alberta's permanent UTC-6 since 2026-06 still showed as "MST").
     */
    fun describe(zone: ZoneId, now: ZonedDateTime = ZonedDateTime.now(zone)): String {
        val time = now.withZoneSameInstant(zone)
        return "${zone.id}, ${time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))}, ${utcOffset(time.offset)}"
    }

    /** "UTC-5", "UTC+5:30", "UTC". */
    fun utcOffset(offset: java.time.ZoneOffset): String {
        val seconds = offset.totalSeconds
        if (seconds == 0) return "UTC"
        val sign = if (seconds < 0) "-" else "+"
        val hours = Math.abs(seconds) / 3600
        val minutes = Math.abs(seconds) % 3600 / 60
        return "UTC$sign$hours" + if (minutes != 0) ":%02d".format(minutes) else ""
    }
}
