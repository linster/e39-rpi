package ca.stefanm.ibus.gui.menu.widgets.bottombar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.yield
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.Duration

class BottomBarClock @Inject constructor() {

    private val _dateFlow : MutableStateFlow<String> = MutableStateFlow("")
    private val _timeFlow : MutableStateFlow<String> = MutableStateFlow("")

    val dateFlow = _dateFlow.asStateFlow()
    val timeFlow = _timeFlow.asStateFlow()

    suspend fun updateValues() {
        while (true) {
            val currentTime = Instant.now().atZone(ZoneId.systemDefault())

            _timeFlow.value = currentTime.toLocalDateTime().let { localDateTime ->
                "${localDateTime.hour.rem(13).toString(10).padStart(2, '0')}:${localDateTime.minute.toString(10).padStart(2, '0')} ${localDateTime.hour.let { if (it >= 12) "PM" else "AM" }}"
            }

            _dateFlow.value = currentTime.let {
                "${it.dayOfMonth}/${it.month.value}/${it.year}"
            }
            delay(Duration.Companion.seconds(15))
            yield()
        }
    }
}