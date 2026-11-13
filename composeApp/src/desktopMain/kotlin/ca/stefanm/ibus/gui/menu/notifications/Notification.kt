package ca.stefanm.ibus.gui.menu

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

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

        // Turn-by-turn instructions (drawable/guidance-*.svg; which maneuver
        // gets which: gui.map.guidance.GuidanceIcons).
        GUIDANCE_CONTINUE,
        GUIDANCE_START,
        GUIDANCE_START_RIGHT,
        GUIDANCE_START_LEFT,
        GUIDANCE_DESTINATION,
        GUIDANCE_DESTINATION_RIGHT,
        GUIDANCE_DESTINATION_LEFT,
        GUIDANCE_SLIGHT_RIGHT,
        GUIDANCE_RIGHT,
        GUIDANCE_SHARP_RIGHT,
        GUIDANCE_UTURN_RIGHT,
        GUIDANCE_UTURN_LEFT,
        GUIDANCE_SHARP_LEFT,
        GUIDANCE_LEFT,
        GUIDANCE_SLIGHT_LEFT,
        GUIDANCE_RAMP_RIGHT,
        GUIDANCE_RAMP_LEFT,
        GUIDANCE_EXIT_RIGHT,
        GUIDANCE_EXIT_LEFT,
        GUIDANCE_STAY_STRAIGHT,
        GUIDANCE_STAY_RIGHT,
        GUIDANCE_STAY_LEFT,
        GUIDANCE_MERGE,
        GUIDANCE_MERGE_RIGHT,
        GUIDANCE_MERGE_LEFT,
        GUIDANCE_ROUNDABOUT_ENTER,
        GUIDANCE_ROUNDABOUT_EXIT,
        GUIDANCE_FERRY_ENTER,
        GUIDANCE_FERRY_EXIT,
    }

    enum class NotificationDuration(val duration: kotlin.time.Duration) {
        SHORT(3.seconds),
        LONG(8.seconds),
        INDEFINITE(Duration.INFINITE)
    }
}