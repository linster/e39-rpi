package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.logger.Logger
import javax.inject.Inject

class NotificationSpammerDebug @Inject constructor(
    private val logger: Logger,
    private val notificationHub: NotificationHub
) : WindowManager.E39Window{

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE
    override val size = DpSize(300.dp, 500.dp)

    override val title = "Notification Spammer"

    override fun content(): @Composable WindowScope.() -> Unit = {

        val isStarted = remember { mutableStateOf(false) }

        LaunchedEffect(isStarted.value) {
            val notifications : List<Notification> =
                Notification.NotificationImage.values().toList().map { image ->
                    Notification(
                        image = image,
                        topText = image.name,
                        contentText = image.name.repeat(2),

                    )
                }

            if (isStarted.value) {
                notifications.forEach {
                    logger.d("NotificationSpammer", "Showing: $it")
                    notificationHub.postNotification(it)
                }
            }
        }

        LaunchedEffect(isStarted.value) {
            if (!isStarted.value) {
                notificationHub.clearNotification()
            }
        }

        Column {

            Text("isStarted: ${isStarted.value}")

            Spacer(Modifier.height(10.dp))

            Row {
                Button(onClick = {
                    isStarted.value = true
                }) { Text("Start") }

                Button(onClick = {
                    isStarted.value = false
                }) { Text("Stop")}
            }

            Spacer(Modifier.height(10.dp))

            val notifications : List<Notification> =
                Notification.NotificationImage.values().toList().map { image ->
                    Notification(
                        image = image,
                        topText = image.name,
                        contentText = image.name.repeat(2),

                        )
                }

            for (notification in notifications) {
                Button(onClick = { notificationHub.postNotificationBackground(notification)}) {
                    Text(notification.topText)
                }
            }

        }
    }
}