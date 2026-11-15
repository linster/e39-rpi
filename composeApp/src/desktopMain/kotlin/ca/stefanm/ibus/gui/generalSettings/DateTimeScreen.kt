package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.system.ClockSettings
import ca.stefanm.ibus.lib.system.ClockStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@ScreenDoc(
    screenName = "DateTimeScreen",
    description = "Date & Time: the time, internet time (NTP) on or off, the date and time set by hand " +
            "with the knob, and the way to the time zone.",
    navigatesTo = [
        ScreenDoc.NavigateTo(TimeZoneScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class DateTimeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val clockSettings: ClockSettings,
) : NavigationNode<Nothing> {

    /** The top level, or the date and time being set by hand. */
    sealed interface Level {
        data object Top : Level
        data object SetByHand : Level
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DateTimeScreen::class.java

    // The date and time being set by hand (kept while the sliders come and go).
    private val draft = mutableStateOf(DateTimeLabels.startingDraft(LocalDateTime.now()))

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val level = params?.requestParameters as? Level ?: Level.Top
        val scope = rememberCoroutineScope()
        val now = remember(params) { mutableStateOf(LocalDateTime.now()) }
        val status = remember(params) { mutableStateOf<ClockStatus?>(null) }

        LaunchedEffect(params) {
            while (true) {
                now.value = LocalDateTime.now()
                status.value = clockSettings.status()
                delay(5_000)
            }
        }

        val items: List<MenuItem> = when (level) {
            Level.Top -> topItems(scope, now.value, status.value)
            Level.SetByHand -> setByHandItems(scope, draft.value)
        }
        val header = when (level) {
            Level.Top -> "Date & Time"
            Level.SetByHand -> "Date & Time: set by hand"
        }

        // Keyed on the structure only (not "Now: ...", which changes every
        // minute, or the values being set): re-creating the menu would move
        // the knob's highlight back to the top.
        key(level, status.value?.internetTime, items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext("DateTimeScreen-$level").OneColumnSmoothScreen(
                header = header,
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun topItems(scope: CoroutineScope, now: LocalDateTime, status: ClockStatus?): List<MenuItem> = buildList {
        add(TextMenuItem("Now: ${DateTimeLabels.describe(now)}", isSelectable = false, onClicked = {}))
        add(TextMenuItem(DateTimeLabels.internetTime(status), isSelectable = false, onClicked = {}))
        if (!clockSettings.canChange()) {
            add(TextMenuItem("Can't be changed on this system", isSelectable = false, onClicked = {}))
            return@buildList
        }
        val automatic = status?.internetTime ?: true
        add(TextMenuItem(TimeZoneLabels.mark(automatic, "Automatic (internet time)"), onClicked = {
            if (!automatic) run(scope, "Internet time on", "Automatic (internet time)") { clockSettings.setInternetTime(true) }
        }))
        add(TextMenuItem(TimeZoneLabels.mark(!automatic, "Set by hand..."), onClicked = {
            draft.value = DateTimeLabels.startingDraft(LocalDateTime.now())
            navigationNodeTraverser.navigateToNodeWithParameters(DateTimeScreen::class.java, Level.SetByHand)
        }))
        add(TextMenuItem("Time zone: ${ZoneId.systemDefault().id}", onClicked = {
            navigationNodeTraverser.navigateToNode(TimeZoneScreen::class.java)
        }))
    }

    private fun setByHandItems(scope: CoroutineScope, value: LocalDateTime): List<MenuItem> = buildList {
        DateTimeLabels.Field.entries.forEach { field ->
            add(TextMenuItem(field.label(value), onClicked = { pick(field) }))
        }
        add(TextMenuItem("Set the clock: ${DateTimeLabels.describe(value)}", onClicked = {
            run(scope, "Clock set", DateTimeLabels.describe(value), thenBack = true) { clockSettings.setTime(value) }
        }))
        add(TextMenuItem("(Turns internet time off)", isSelectable = false, onClicked = {}))
    }

    private fun pick(field: DateTimeLabels.Field) {
        val start = field.get(draft.value)
        modalMenuService.showIntSlider(
            initialValue = start,
            validItems = field.range(draft.value),
            hintText = field.title,
            onCurrentValueChanged = { draft.value = field.with(draft.value, it) },
        )
    }

    private fun run(
        scope: CoroutineScope,
        what: String,
        description: String,
        thenBack: Boolean = false,
        change: suspend () -> Result<Unit>,
    ) {
        scope.launch {
            change().fold(
                onSuccess = {
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.MESSAGE_CIRCLE, what, description)
                    )
                    if (thenBack) navigationNodeTraverser.goBack()
                },
                onFailure = {
                    notificationHub.postNotificationBackground(
                        Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Time not changed",
                            it.message ?: it.toString(), Notification.NotificationDuration.LONG)
                    )
                }
            )
        }
    }
}

/** Text and rules for the date and time screen (no Compose: unit-tested). */
object DateTimeLabels {
    private val DESCRIBE = DateTimeFormatter.ofPattern("EEE d MMM yyyy, h:mm a", Locale.ENGLISH)

    /** "Sun 15 Nov 2026, 9:07 AM". */
    fun describe(time: LocalDateTime): String = time.format(DESCRIBE)

    fun internetTime(status: ClockStatus?): String = when {
        status == null -> "Internet time: unknown"
        !status.internetTime -> "Internet time: off"
        status.synchronized -> "Internet time: synchronized"
        else -> "Internet time: not synchronized yet"
    }

    /** To the minute: the helper sets hours and minutes. */
    fun startingDraft(now: LocalDateTime): LocalDateTime = now.withSecond(0).withNano(0)

    enum class Field(val title: String) {
        YEAR("Year"), MONTH("Month"), DAY("Day"), HOUR("Hour"), MINUTE("Minute");

        fun get(t: LocalDateTime): Int = when (this) {
            YEAR -> t.year; MONTH -> t.monthValue; DAY -> t.dayOfMonth; HOUR -> t.hour; MINUTE -> t.minute
        }

        /** The slider's range; a day within the month's length. */
        fun range(t: LocalDateTime): IntProgression = when (this) {
            YEAR -> 2024..2050
            MONTH -> 1..12
            DAY -> 1..t.toLocalDate().lengthOfMonth()
            HOUR -> 0..23
            MINUTE -> 0..59
        }

        /** [t] with this field set; the day shrinks to fit a shorter month (Jan 31 -> Feb 28). */
        fun with(t: LocalDateTime, value: Int): LocalDateTime = when (this) {
            YEAR -> t.withDayOfMonth(1).withYear(value).let { it.withDayOfMonth(minOf(t.dayOfMonth, it.toLocalDate().lengthOfMonth())) }
            MONTH -> t.withDayOfMonth(1).withMonth(value).let { it.withDayOfMonth(minOf(t.dayOfMonth, it.toLocalDate().lengthOfMonth())) }
            DAY -> t.withDayOfMonth(value.coerceIn(1, t.toLocalDate().lengthOfMonth()))
            HOUR -> t.withHour(value)
            MINUTE -> t.withMinute(value)
        }

        fun label(t: LocalDateTime): String = when (this) {
            YEAR -> "Year: ${t.year}"
            MONTH -> "Month: ${t.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
            DAY -> "Day: ${t.dayOfMonth} (${t.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)})"
            HOUR -> "Hour: %02d".format(t.hour)
            MINUTE -> "Minute: %02d".format(t.minute)
        }
    }
}
