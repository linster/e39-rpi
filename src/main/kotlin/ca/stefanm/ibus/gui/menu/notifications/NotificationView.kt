package ca.stefanm.ibus.gui.menu.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.Notification
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable fun Notification.toView() {
    Row(
        Modifier.fillMaxSize()
            .background(Color(48, 72, 107, 255))
    ) {
        if (this@toView.image == Notification.NotificationImage.NONE) {
            Column(
                Modifier.padding(15.dp)
            ) {
                Text(
                    text = this@toView.topText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (this@toView.contentText.isNotEmpty()) {
                    Text(
                        text = this@toView.contentText,
                        fontSize = 36.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Column(
                Modifier
                    .weight(0.20F)
                    .border(2.dp, Color.Cyan)
                    .align(Alignment.CenterVertically)
            ) {
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    //image here
                    //https://feathericons.com/?query=nav
                    val resource = svgResource(
                        when (this@toView.image) {
                            Notification.NotificationImage.NONE -> error("Invalid")
                            Notification.NotificationImage.ALERT_CIRCLE -> "icons/alert-circle.png"
                            Notification.NotificationImage.ALERT_OCTAGON -> "icons/alert-octagon.png"
                            Notification.NotificationImage.ALERT_TRIANGLE -> "icons/alert-triangle.png"
                            Notification.NotificationImage.BLUETOOTH -> "icons/bluetooth.svg"
                            Notification.NotificationImage.MESSAGE_CIRCLE -> "icons/message-circle.svg"
                            Notification.NotificationImage.MESSAGE_SQUARE -> "icons/message-square.svg"
                            Notification.NotificationImage.MUSIC -> "icons/music.svg"
                            Notification.NotificationImage.PHONE -> "icons/phone.svg"
                            Notification.NotificationImage.PHONE_INCOMING -> "icons/phone-incoming.svg"
                            Notification.NotificationImage.PHONE_MISSED -> "icons/phone-missed.svg"
                            Notification.NotificationImage.VOICE_MAIL -> "icons/voicemail.svg"
                            Notification.NotificationImage.MAP_GENERAL -> "icons/map.svg"
                            Notification.NotificationImage.MAP_INSTRUCTION -> "icons/navigation.svg"
                            Notification.NotificationImage.MAP_WAYPOINT -> "icons/map-pin.svg"
                        }
                    )
                    Image(
                        painter = resource,
                        contentDescription = this@toView.image.toString(),
                        modifier = Modifier
                            .fillMaxSize(0.75F)
                            .border(2.dp, Color.Red)
                            .aspectRatio(1.0F)
                    )
                }
            }

            Column(
                Modifier.weight(0.75F)
                    .padding(15.dp)
            ) {
                Text(
                    text = this@toView.topText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (this@toView.contentText.isNotEmpty()) {
                    Text(
                        text = this@toView.contentText,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}