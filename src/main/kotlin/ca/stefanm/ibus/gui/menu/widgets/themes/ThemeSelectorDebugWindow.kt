package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IbusInputEventCliPrinter
import javax.inject.Inject
import javax.inject.Provider

class ThemeSelectorDebugWindow @Inject constructor(
    private val themeConfigurationStorage: ThemeConfigurationStorage,
    private val windowManager: Provider<WindowManager>,
    private val notificationHub: NotificationHub
): WindowManager.E39Window {

    override val title: String = "Theme Selector"
    override val size = DpSize(800.dp, 600.dp)
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        NestingCard() {
            NestingCard {
                NestingCardHeader("HMI Window")
                Button(onClick = {
                    windowManager.get().closeHmiMainWindow()
                }) { Text("Close HMI") }
                Button(onClick = {
                    windowManager.get().openHmiMainWindow()
                }) { Text("Open HMI") }
            }


            NestingCard {
                NestingCardHeader("Select Theme Preset")
                Themes.availableThemes.forEach {
                    Button(onClick = {
                        themeConfigurationStorage.setTheme(it)
                    }) { Text(it.friendlyName) }
                }
            }

            NestingCard {
                Button(onClick = {
                    notificationHub.postNotificationBackground(
                        Notification(
                            Notification.NotificationImage.ALERT_CIRCLE,
                            topText = "Test Notification",
                            contentText = "This is a notification test thing."
                        )
                    )
                }) { Text("Post Notification")}
            }
        }
    }
}