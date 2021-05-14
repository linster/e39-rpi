package ca.stefanm.ibus.gui.menu

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
data class Notification(
    val image: NotificationImage = NotificationImage.NONE,
    val topText : String,
    val contentText : String = "",
    val duration: NotificationDuration = NotificationDuration.SHORT
) {
    enum class NotificationImage {
        NONE,
        ALERT_CIRCLE,
        ALERT_OCTAGON,
        ALERT_TRIANGLE,
        BLUETOOTH,
        MESSAGE_CIRCLE,
        MESSAGE_SQUARE,
        MUSIC,
        PHONE,
        PHONE_INCOMING,
        PHONE_MISSED,
        VOICE_MAIL,

        MAP_GENERAL,
        MAP_INSTRUCTION,
        MAP_WAYPOINT,
    }

    enum class NotificationDuration(val duration: kotlin.time.Duration) {
        SHORT(3.seconds),
        LONG(8.seconds),
    }
}