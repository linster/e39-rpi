package ca.stefanm.ibus.gui.menu.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.Notification

@Composable fun Notification.toView() {
    val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled

    Row(
        Modifier.fillMaxSize()
            //.background(Color(48, 72, 107, 255))
            .border(
                width = if (isPixelDoubled) 2.dp else 1.dp,
                color = ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .background(
                Brush.horizontalGradient(
                    ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.backgroundGradientColorList
                )
            )
    ) {
        if (this@toView.image == Notification.NotificationImage.NONE) {
            Column(
                Modifier.padding(
                    if (isPixelDoubled) 16.dp else 8.dp
                )
            ) {
                if (this@toView.topText.isNotEmpty()) {
                    Text(
                        text = this@toView.topText,
                        fontSize = if (isPixelDoubled) 48.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (this@toView.contentText.isNotEmpty()) {
                    Text(
                        text = this@toView.contentText,
                        fontSize = if(isPixelDoubled) 36.sp else 18.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Column(
                Modifier
                    .weight(0.20F)
                    //.border(2.dp, Color.Cyan)
                    .align(Alignment.CenterVertically)
            ) {
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    //image here
                    //https://feathericons.com/?query=nav
                    val resource = painterResource(
                        when (this@toView.image) {
                            Notification.NotificationImage.NONE -> error("Invalid")
                            Notification.NotificationImage.ALERT_CIRCLE -> "icons/alert-circle.svg"
                            Notification.NotificationImage.ALERT_OCTAGON -> "icons/alert-octagon.svg"
                            Notification.NotificationImage.ALERT_TRIANGLE -> "icons/alert-triangle.svg"
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
                            //.border(2.dp, Color.Red)
                            .aspectRatio(1.0F)
                    )
                }
            }

                Column(
                    Modifier.weight(0.75F)
                        .padding(if (isPixelDoubled) 16.dp else 8.dp)
                ) {
                    if (this@toView.topText.isNotEmpty()) {
                        Text(
                            text = this@toView.topText,
                            fontSize = if (isPixelDoubled) 32.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    if (this@toView.contentText.isNotEmpty()) {
                        Text(
                            text = this@toView.contentText,
                            fontSize = if (isPixelDoubled) 26.sp else 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
}