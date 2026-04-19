package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import javax.inject.Inject

class DisconnectNmtDeviceConnectionUseCase @Inject constructor(
    private val logger: Logger,
    private val notificationHub: NotificationHub
) {

    companion object {
        const val TAG = "DisconnectNmtDeviceConnectionUseCase"
    }

    fun disconnect(conn : Nmt.NmtConnectConnection) {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        connection.use {
            val nmClient = connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )

            try {
                conn.active?.let {
                    nmClient.DeactivateConnection(
                        DBusPath(it.objectPath)
                    )
                }
            } catch (e: Throwable) {
                logger.e(TAG, "Could not deactivate connection $conn", e)
                notificationHub.postNotificationBackground(
                    Notification(
                        Notification.NotificationImage.ALERT_TRIANGLE,
                        "Error Deactivating connection ${conn.name}",
                        contentText = "${e.message}"
                    )
                )
            }
        }
    }
}