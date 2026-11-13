package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository.ManeuverKind
import ca.stefanm.ibus.gui.menu.Notification.NotificationImage
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * The pictures for guidance instructions (drawable/guidance-*.svg, in the
 * notification icons' style): which one each maneuver kind gets, as a
 * notification image and as a drawable (notifications, the side panel).
 */
object GuidanceIcons {

    fun notificationImage(kind: ManeuverKind): NotificationImage = when (kind) {
        ManeuverKind.START -> NotificationImage.GUIDANCE_START
        ManeuverKind.START_RIGHT -> NotificationImage.GUIDANCE_START_RIGHT
        ManeuverKind.START_LEFT -> NotificationImage.GUIDANCE_START_LEFT
        ManeuverKind.DESTINATION -> NotificationImage.GUIDANCE_DESTINATION
        ManeuverKind.DESTINATION_RIGHT -> NotificationImage.GUIDANCE_DESTINATION_RIGHT
        ManeuverKind.DESTINATION_LEFT -> NotificationImage.GUIDANCE_DESTINATION_LEFT
        ManeuverKind.UNSPECIFIED, ManeuverKind.NONE, ManeuverKind.BECOMES, ManeuverKind.CONTINUE,
        ManeuverKind.RAMP_STRAIGHT -> NotificationImage.GUIDANCE_CONTINUE
        ManeuverKind.SLIGHT_RIGHT -> NotificationImage.GUIDANCE_SLIGHT_RIGHT
        ManeuverKind.RIGHT -> NotificationImage.GUIDANCE_RIGHT
        ManeuverKind.SHARP_RIGHT -> NotificationImage.GUIDANCE_SHARP_RIGHT
        ManeuverKind.UTURN_RIGHT -> NotificationImage.GUIDANCE_UTURN_RIGHT
        ManeuverKind.UTURN_LEFT -> NotificationImage.GUIDANCE_UTURN_LEFT
        ManeuverKind.SHARP_LEFT -> NotificationImage.GUIDANCE_SHARP_LEFT
        ManeuverKind.LEFT -> NotificationImage.GUIDANCE_LEFT
        ManeuverKind.SLIGHT_LEFT -> NotificationImage.GUIDANCE_SLIGHT_LEFT
        ManeuverKind.RAMP_RIGHT -> NotificationImage.GUIDANCE_RAMP_RIGHT
        ManeuverKind.RAMP_LEFT -> NotificationImage.GUIDANCE_RAMP_LEFT
        ManeuverKind.EXIT_RIGHT -> NotificationImage.GUIDANCE_EXIT_RIGHT
        ManeuverKind.EXIT_LEFT -> NotificationImage.GUIDANCE_EXIT_LEFT
        ManeuverKind.STAY_STRAIGHT -> NotificationImage.GUIDANCE_STAY_STRAIGHT
        ManeuverKind.STAY_RIGHT -> NotificationImage.GUIDANCE_STAY_RIGHT
        ManeuverKind.STAY_LEFT -> NotificationImage.GUIDANCE_STAY_LEFT
        ManeuverKind.MERGE -> NotificationImage.GUIDANCE_MERGE
        ManeuverKind.MERGE_RIGHT -> NotificationImage.GUIDANCE_MERGE_RIGHT
        ManeuverKind.MERGE_LEFT -> NotificationImage.GUIDANCE_MERGE_LEFT
        ManeuverKind.ROUNDABOUT_ENTER -> NotificationImage.GUIDANCE_ROUNDABOUT_ENTER
        ManeuverKind.ROUNDABOUT_EXIT -> NotificationImage.GUIDANCE_ROUNDABOUT_EXIT
        ManeuverKind.FERRY_ENTER -> NotificationImage.GUIDANCE_FERRY_ENTER
        ManeuverKind.FERRY_EXIT -> NotificationImage.GUIDANCE_FERRY_EXIT
    }

    /** The drawable of a GUIDANCE_* image; null for the other notification images. */
    fun drawable(image: NotificationImage): DrawableResource? = when (image) {
        NotificationImage.GUIDANCE_CONTINUE -> Res.drawable.guidance_continue
        NotificationImage.GUIDANCE_START -> Res.drawable.guidance_start
        NotificationImage.GUIDANCE_START_RIGHT -> Res.drawable.guidance_start_right
        NotificationImage.GUIDANCE_START_LEFT -> Res.drawable.guidance_start_left
        NotificationImage.GUIDANCE_DESTINATION -> Res.drawable.guidance_destination
        NotificationImage.GUIDANCE_DESTINATION_RIGHT -> Res.drawable.guidance_destination_right
        NotificationImage.GUIDANCE_DESTINATION_LEFT -> Res.drawable.guidance_destination_left
        NotificationImage.GUIDANCE_SLIGHT_RIGHT -> Res.drawable.guidance_slight_right
        NotificationImage.GUIDANCE_RIGHT -> Res.drawable.guidance_right
        NotificationImage.GUIDANCE_SHARP_RIGHT -> Res.drawable.guidance_sharp_right
        NotificationImage.GUIDANCE_UTURN_RIGHT -> Res.drawable.guidance_uturn_right
        NotificationImage.GUIDANCE_UTURN_LEFT -> Res.drawable.guidance_uturn_left
        NotificationImage.GUIDANCE_SHARP_LEFT -> Res.drawable.guidance_sharp_left
        NotificationImage.GUIDANCE_LEFT -> Res.drawable.guidance_left
        NotificationImage.GUIDANCE_SLIGHT_LEFT -> Res.drawable.guidance_slight_left
        NotificationImage.GUIDANCE_RAMP_RIGHT -> Res.drawable.guidance_ramp_right
        NotificationImage.GUIDANCE_RAMP_LEFT -> Res.drawable.guidance_ramp_left
        NotificationImage.GUIDANCE_EXIT_RIGHT -> Res.drawable.guidance_exit_right
        NotificationImage.GUIDANCE_EXIT_LEFT -> Res.drawable.guidance_exit_left
        NotificationImage.GUIDANCE_STAY_STRAIGHT -> Res.drawable.guidance_stay_straight
        NotificationImage.GUIDANCE_STAY_RIGHT -> Res.drawable.guidance_stay_right
        NotificationImage.GUIDANCE_STAY_LEFT -> Res.drawable.guidance_stay_left
        NotificationImage.GUIDANCE_MERGE -> Res.drawable.guidance_merge
        NotificationImage.GUIDANCE_MERGE_RIGHT -> Res.drawable.guidance_merge_right
        NotificationImage.GUIDANCE_MERGE_LEFT -> Res.drawable.guidance_merge_left
        NotificationImage.GUIDANCE_ROUNDABOUT_ENTER -> Res.drawable.guidance_roundabout_enter
        NotificationImage.GUIDANCE_ROUNDABOUT_EXIT -> Res.drawable.guidance_roundabout_exit
        NotificationImage.GUIDANCE_FERRY_ENTER -> Res.drawable.guidance_ferry_enter
        NotificationImage.GUIDANCE_FERRY_EXIT -> Res.drawable.guidance_ferry_exit
        else -> null
    }

    fun drawable(kind: ManeuverKind): DrawableResource = drawable(notificationImage(kind))!!
}
