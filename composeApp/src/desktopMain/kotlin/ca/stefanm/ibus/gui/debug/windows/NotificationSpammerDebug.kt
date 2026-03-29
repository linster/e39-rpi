package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class NotificationSpammerDebug @Inject constructor(
    private val logger: Logger,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService
) : WindowManager.E39Window{

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE
    override val size = DpSize(800.dp, 1500.dp)

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

        Row {
            NestingCard {
                NestingCardHeader("Notification Spammer")
                Text("isStarted: ${isStarted.value}")

                Spacer(Modifier.height(10.dp))

                Row {
                    Button(onClick = {
                        isStarted.value = true
                    }) { Text("Start") }

                    Button(onClick = {
                        isStarted.value = false
                    }) { Text("Stop") }
                }

                Spacer(Modifier.height(10.dp))

                val notifications: List<Notification> =
                    Notification.NotificationImage.values().toList().map { image ->
                        Notification(
                            image = image,
                            topText = image.name,
                            contentText = image.name.repeat(2),

                            )
                    }

                for (notification in notifications) {
                    Button(onClick = { notificationHub.postNotificationBackground(notification) }) {
                        Text(notification.topText)
                    }
                }

            }

            NestingCard {
                NestingCardHeader("Modal Wait Dialog Tester")

                val isCancellable = remember { mutableStateOf(false)}
                CheckBoxWithLabel(
                    isCancellable.value,
                    onCheckChanged = { isCancellable.value = it },
                    "isCancellable?"
                )

                val autoCloseTimeout = remember { mutableStateOf<Int?>(null) }

                NumericTextViewWithSpinnerButtons(
                    label = "AutoClose in seconds",
                    initialValue = 0,
                    onValueChanged = { autoCloseTimeout.value = it}
                )

                Text("Autoclose: ${if (autoCloseTimeout.value == null) "null" else autoCloseTimeout.value}")
                Button(onClick = {
                    autoCloseTimeout.value = null
                }) { Text("Set AutoClose to null")}

                val selectedImage = remember { mutableStateOf(Notification.NotificationImage.NONE) }
                NestingCard {
                    NestingCardHeader("Select Image for Modal")
                    Column(modifier = Modifier.selectableGroup()) {
                        for (image in Notification.NotificationImage.values()) {
                            Row(modifier = Modifier.selectable(
                                selected = selectedImage.value == image,
                                onClick = { selectedImage.value = image },
                                role = Role.RadioButton
                            )) {
                                RadioButton(
                                    selectedImage.value == image,
                                    onClick = null
                                )
                                Text(image.name)
                            }
                        }
                    }
                }

                val headerText = remember { mutableStateOf("Please Wait") }
                Row {
                    Text("Header Text")
                    TextField(
                        value = headerText.value.toString(),
                        onValueChange = { newStr ->
                            headerText.value = newStr

                        },
                        modifier = Modifier.width(128.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                }

                val bodyText = remember { mutableStateOf("") }
                Row {
                    Text("Body Text")
                    TextField(
                        value = bodyText.value.toString(),
                        onValueChange = { newStr ->
                            bodyText.value = newStr

                        },
                        modifier = Modifier.width(128.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                }

                Button(onClick = {
                    modalMenuService.showModalWaitDialog(
                        image = selectedImage.value,
                        headerText = headerText.value,
                        bodyText = bodyText.value,
                        autoCloseTimeout = autoCloseTimeout.value?.seconds
                    )
                }) { Text("Show") }
                Button(onClick = {
                    modalMenuService.closeModalMenu()
                }) { Text("Hide") }
            }
        }
    }
}