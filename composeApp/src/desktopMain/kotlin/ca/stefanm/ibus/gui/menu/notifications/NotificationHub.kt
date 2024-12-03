package ca.stefanm.ca.stefanm.ibus.gui.menu.notifications

import androidx.compose.runtime.MutableState
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@ApplicationScope
class NotificationHub @Inject constructor() {

    private val _currentNotification = MutableStateFlow<Notification?>(null)
    private val _currentNotificationIsVisible = MutableStateFlow(false)

    val currentNotification
        get() = _currentNotification.asStateFlow()
    val currentNotificationIsVisible
        get() = _currentNotificationIsVisible.asStateFlow()

    fun postNotificationBackground(notification: Notification) {
        GlobalScope.launch {
            postNotification(notification)
        }
    }

    suspend fun postNotification(notification: Notification) {
        _currentNotification.value = notification
        _currentNotificationIsVisible.value = true
        delay(notification.duration.duration)
        _currentNotificationIsVisible.value = false
    }

    fun clearNotification() {
        _currentNotificationIsVisible.value = false
        _currentNotification.value = null
    }
}