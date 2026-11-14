package ca.stefanm.ibus.gui.menu.widgets.bottombar

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.system.TimeZoneSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.yield
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BottomBarClock @Inject constructor(
    private val timeZoneSettings: TimeZoneSettings,
) {

    private val _dateFlow : MutableStateFlow<String> = MutableStateFlow("")
    private val _timeFlow : MutableStateFlow<String> = MutableStateFlow("")

    val dateFlow = _dateFlow.asStateFlow()
    val timeFlow = _timeFlow.asStateFlow()

    suspend fun updateValues() {
        while (true) {
            // The system's zone may have changed (Settings -> Time Zone does
            // it in-process; the map installer or position don't).
            timeZoneSettings.followSystemZone()
            val currentTime = Instant.now().atZone(ZoneId.systemDefault())

            _timeFlow.value = clockText(currentTime.toLocalDateTime())

            _dateFlow.value = currentTime.let {
                "${it.dayOfMonth}/${it.month.value}/${it.year}"
            }
            delay(45.seconds)
            yield()
        }
    }
}

/** "02:36 PM"; 12 for noon and midnight (12:05 AM), not 00. */
internal fun clockText(time: java.time.LocalDateTime): String {
    val hour12 = (time.hour + 11) % 12 + 1
    return "${hour12.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')} ${if (time.hour >= 12) "PM" else "AM"}"
}
