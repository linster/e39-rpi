package ca.stefanm.ibus.gui.networkSetup.editConnection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.WifiSecurity
import ca.stefanm.ibus.gui.networkSetup.editConnection.dbus.SavedConnectionsUseCase
import ca.stefanm.ibus.gui.networkSetup.editConnection.dbus.SavedConnectionsUseCase.SavedConnection
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "EditConnectionScreen",
    description = "Lists the connection profiles NetworkManager has saved, and lets them be renamed, " +
            "set to connect automatically, have their wifi password changed, or be deleted."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class EditConnectionScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val savedConnectionsUseCase: SavedConnectionsUseCase,
    private val logger: Logger
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "EditConnectionScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = EditConnectionScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Edit a Connection")

            val savedConnections = remember { savedConnectionsUseCase.getSavedConnections() }
                .collectAsState(emptyList())

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = TAG,
                logger = logger,
                prependGoBackEntry = true,
                navigationNodeTraverser = navigationNodeTraverser,
                items = savedConnections.value.map { saved ->
                    TextMenuItem(
                        title = "${saved.id} (${SavedConnectionsUseCase.prettyType(saved.type)})" +
                                if (saved.autoconnect) "" else " - manual",
                        onClicked = { openSidebarForConnection(saved) }
                    )
                }.toDynamicLambdas()
            )
        }
    }

    private fun openSidebarForConnection(saved : SavedConnection) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Connection",
                @Composable {
                    InfoLabel("Name: ${saved.id}")
                    InfoLabel("Type: ${SavedConnectionsUseCase.prettyType(saved.type)}")
                    if (saved.ssid != null) {
                        InfoLabel("SSID: ${saved.ssid}")
                    }
                    InfoLabel("Interface: ${saved.interfaceName ?: "Any"}")
                    InfoLabel("Connect automatically: ${if (saved.autoconnect) "Yes" else "No"}")
                    InfoLabel("UUID: ${saved.uuid ?: "Unknown"}")
                },
                listOfNotNull(
                    TextMenuItem("Rename", onClicked = { rename(saved) }),
                    TextMenuItem(
                        if (saved.autoconnect) "Don't Connect Automatically" else "Connect Automatically",
                        onClicked = { setAutoconnect(saved, !saved.autoconnect) }
                    ),
                    if (saved.hasWifiPassword) {
                        TextMenuItem("Change Password", onClicked = { changePassword(saved) })
                    } else null,
                    TextMenuItem("Delete", onClicked = { confirmDelete(saved) }),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) })
                )
            )
        }
    }

    private fun confirmDelete(saved : SavedConnection) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Delete Connection?",
                @Composable {
                    InfoLabel("Delete ${saved.id}?")
                    InfoLabel("This can't be undone.")
                },
                listOf(
                    TextMenuItem("Delete", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        runEdit(saved, "Deleted connection") {
                            savedConnectionsUseCase.delete(saved.path)
                        }
                    }),
                    TextMenuItem("Cancel", onClicked = { openSidebarForConnection(saved) })
                )
            )
        }
    }

    private fun rename(saved : SavedConnection) {
        modalMenuService.closeSidePaneOverlay(true)
        modalMenuService.showKeyboard(
            type = Keyboard.KeyboardType.FULL,
            prefilled = saved.id,
            onTextEntered = { newName ->
                if (newName.isBlank() || newName == saved.id) {
                    return@showKeyboard
                }
                runEdit(saved, "Renamed to $newName") {
                    savedConnectionsUseCase.rename(saved.path, newName)
                }
            }
        )
    }

    private fun setAutoconnect(saved : SavedConnection, autoconnect : Boolean) {
        modalMenuService.closeSidePaneOverlay(true)
        runEdit(saved, if (autoconnect) "Will connect automatically" else "Won't connect automatically") {
            savedConnectionsUseCase.setAutoconnect(saved.path, autoconnect)
        }
    }

    private fun changePassword(saved : SavedConnection) {
        val security = if (saved.keyMgmt == "sae") WifiSecurity.SAE else WifiSecurity.WPA_PSK
        modalMenuService.closeSidePaneOverlay(true)
        modalMenuService.showKeyboard(
            type = Keyboard.KeyboardType.FULL,
            onTextEntered = { password ->
                if (!security.isPasswordValid(password)) {
                    notificationHub.postNotificationBackground(Notification(
                        image = Notification.NotificationImage.ALERT_TRIANGLE,
                        topText = "Password not changed",
                        contentText = if (security == WifiSecurity.WPA_PSK) {
                            "WPA passwords are 8 to 63 characters long."
                        } else {
                            "The password can't be empty."
                        }
                    ))
                    return@showKeyboard
                }
                runEdit(saved, "Password changed") {
                    savedConnectionsUseCase.setWifiPassword(saved.path, password)
                }
            }
        )
    }

    private fun runEdit(saved : SavedConnection, successText : String, edit : () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                edit()
                notificationHub.postNotificationBackground(Notification(
                    topText = saved.id,
                    contentText = successText
                ))
            } catch (e : Throwable) {
                logger.e(TAG, "Editing ${saved.id} failed", e)
                notificationHub.postNotificationBackground(Notification(
                    image = Notification.NotificationImage.ALERT_TRIANGLE,
                    topText = "Could not edit ${saved.id}",
                    contentText = e.message ?: ""
                ))
            }
        }
    }
}
