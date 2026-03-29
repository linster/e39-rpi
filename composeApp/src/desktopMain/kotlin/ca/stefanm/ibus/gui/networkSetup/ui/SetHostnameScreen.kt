package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.github.hypfvieh.DbusHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBus
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Introspectable
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.messages.DBusSignal
import org.freedesktop.networkmanager.Settings
import javax.inject.Inject

@ScreenDoc(
    screenName = "SetHostnameScreen",
    description = "Set the system hostname"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class SetHostnameScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SetHostnameScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        val hostName = getHostname().collectAsState("<<>>")


        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Hostname")

//            key(hostName.value) {
                FullScreenMenu.OneColumn(
                    listOf(
                        TextMenuItem(
                            title = "Hostname: ${hostName.value}",
                            isSelectable = false,
                            onClicked = {}
                        ),
                        TextMenuItem(
                            title = "Change Hostname...",
                            onClicked = {
                                modalMenuService.showKeyboard(
                                    type = Keyboard.KeyboardType.FULL,
                                    prefilled = hostName.value,
                                    onTextEntered = { new ->
                                        saveHostname(new)
                                        modalMenuService.closeModalMenu()
                                    }
                                )
                            }
                        ),

                        TextMenuItem(
                            title = "Go Back",
                            onClicked = {
                                navigationNodeTraverser.goBack()
                            }
                        ),
                    )
                )
//            }
        }
    }

    fun saveHostname(new : String) {
        val connection = DBusConnectionBuilder.forSystemBus().build()

        try {
            connection.connect()
            connection.use {
                val settings = connection.getRemoteObject(
                    "org.freedesktop.NetworkManager",
                    "/org/freedesktop/NetworkManager/Settings",
                    Settings::class.java
                )

                settings.SaveHostname(new)
            }
        } catch (t : Throwable) {
            logger.e("SetHostnameScreen", "Save hostname failed: $t")
            notificationHub.postNotificationBackground(
                Notification(
                    Notification.NotificationImage.ALERT_TRIANGLE,
                    "Save Hostname Failed",
                    t.message ?: ""
                )
            )
        }
    }

    fun getHostname() : Flow<String> {
        return callbackFlow {
            val connection = DBusConnectionBuilder.forSystemBus().build()


            connection.connect()

            val settings = connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager/Settings",
                Settings::class.java
            )

            send(settings.hostname)

            val handler = object : DBusSigHandler<Properties.PropertiesChanged> {
                override fun handle(_signal: Properties.PropertiesChanged?) {
                    this@callbackFlow.trySend(
                        _signal
                            ?.propertiesChanged
                            ?.getOrDefault("Hostname", "<<no update>>") as? String
                            ?: "no signal"
                    )
                }

            }

            val busId = connection.getRemoteObject(
                "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
            )?.GetNameOwner("org.freedesktop.NetworkManager")

            connection.addSigHandler(Properties.PropertiesChanged::class.java, busId, handler)

            awaitClose {
                connection.removeSigHandler(
                    Properties.PropertiesChanged::class.java,
                    handler
                )
                connection.close()
            }
        }.onStart {
            //emit("<null>")
        }.catch {
            logger.e("SetHostnameScreen", "Error in getHostname", it)
            notificationHub.postNotificationBackground(
                Notification(
                    Notification.NotificationImage.ALERT_TRIANGLE,
                    "Get Hostname Failed",
                    it.message ?: ""
                )
            )
            emit("<Error: ${it.message}>")
        }
            .distinctUntilChanged()
    }


}