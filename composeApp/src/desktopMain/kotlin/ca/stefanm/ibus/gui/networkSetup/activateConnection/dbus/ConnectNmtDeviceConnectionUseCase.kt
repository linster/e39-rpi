package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMActiveConnectionState
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetActiveConnectionsUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.Throbbers
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

class ConnectNmtDeviceConnectionUseCase @Inject constructor(
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val throbbers: Throbbers,
    private val getActiveConnectionsUseCase: GetActiveConnectionsUseCase
) {

    companion object {
        const val TAG = "ConnectNmtDeviceConnectionUseCase"
    }
    suspend fun connect(conn : Nmt.NmtConnectConnection) {
        //https://networkmanager.dev/docs/api/latest/gdbus-org.freedesktop.NetworkManager.html#gdbus-method-org-freedesktop-NetworkManager.ActivateConnection

        throbbers.showConnectingThrobber(bodyText = "Starting connection") { }
        logger.i(TAG, "Starting connection flow for $conn")
        if (conn.deviceIsWifi == true) {
            activateWifi(conn)
        } else if (conn.deviceIsVpn == true) {
            activateVpn(conn)
        } else {
            activateRegular(conn)
        }


        //Might have to listen to StateChanged signal to see if we're done
        //might also have to make this a suspend fun

    }




    internal suspend fun activateRegular(conn : Nmt.NmtConnectConnection) {
        val result = activateConnection(
            device = DBusPath(conn.device!!.objectPath),
            connection = DBusPath(conn.conn!!.objectPath),
            specificObject = DBusPath("/")
        )
        if (result.isFailure) {
            result.exceptionOrNull()?.let {
                logger.e(TAG, "Failed to activate connection", it)
                modalMenuService.showModalWaitDialog(
                    image = Notification.NotificationImage.ALERT_TRIANGLE,
                    throbber = false,
                    headerText = "",
                    bodyText = "${it.message}",
                    autoCloseTimeout = 15.seconds,
                    isCancellable = true,
                    onCancel = {
                        modalMenuService.closeModalMenu()
                    }
                )

            }
            return
        }
        val newConn = result.getOrThrow()
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.NONE,
            throbber = true,
            headerText = "Connecting",
            bodyText = "Got new Active Connection",
            autoCloseTimeout = null,
            isCancellable = false,
        )
        throbbers.showConnectingThrobber(bodyText = "Got new ActiveConnection") {
            notificationHub.postNotificationBackground(Notification(
                topText = "Connecting timed out"
            ))
        }
        try {
            getActiveConnectionsUseCase
                .getActiveConnection(newConn)
                .let { with(getActiveConnectionsUseCase) { it.getReactiveActive()} }
                .map { NMActiveConnectionState.fromInt(it.state) }
                .catch {
                    logger.e(TAG, "Error getting activating state", it)
                    emit(NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_UNKNOWN)
                }
                .takeWhile { it != NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED }
                .timeout(30.seconds)
                .catch {
                    if (it is TimeoutCancellationException) {
                        modalMenuService.showModalWaitDialog(
                            image = Notification.NotificationImage.NONE,
                            throbber = false,
                            headerText = "Error connecting",
                            bodyText = "No status update received",
                            autoCloseTimeout = null,
                            isCancellable = true,
                            onCancel = {
                                modalMenuService.closeModalMenu()
                            }
                        )
                    } else {
                        throw it
                    }
                }.onCompletion {
                    if (it != null) {
                        notificationHub.postNotification(Notification(
                            topText = "Error Connecting",
                            contentText = "${it.message}"
                        ))
                        logger.e(TAG, "Error connecting", it)
                    }
                    modalMenuService.closeModalMenu()
                }.collect {
                    modalMenuService.showModalWaitDialog(
                        image = Notification.NotificationImage.NONE,
                        throbber = false,
                        headerText = "Connecting",
                        bodyText = it.let {
                            when(it) {
                                NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_UNKNOWN -> "Unknown"
                                NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATING -> "Activating"
                                NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED -> "Activated"
                                NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATING -> "Deactivating"
                                NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATED -> "Deactivated"
                            }
                        },
                        autoCloseTimeout = null,
                        isCancellable = false,
                    )
                }
        } catch (e : Throwable) {
            modalMenuService.closeModalMenu()
            notificationHub.postNotification(Notification(
                topText = "Error Connecting",
                contentText = "${e.message}"
            ))
            logger.e(TAG, "Error connecting", e)
        }
    }

    internal fun activateVpn(conn : Nmt.NmtConnectConnection) : Result<DBusPath> {
        TODO()
    }

    internal fun activateWifi(conn : Nmt.NmtConnectConnection) : Result<DBusPath> {
        val newConn = activateConnection(
            device = DBusPath(conn.device!!.objectPath),
            connection = DBusPath(conn.conn!!.objectPath),
            specificObject = DBusPath(conn.ap!!.objectPath)
        )
        //TODO register an AgentManager so that the user can enter a password if needed

        return newConn
    }

    internal fun activateConnection(
        connection : DBusPath,
        device : DBusPath,
        specificObject : DBusPath
    ) : Result<DBusPath>{
        return DBusConnectionBuilder.forSystemBus().build().use {
            val nmClient = it.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )

            runCatching {
                nmClient.ActivateConnection(
                    connection,
                    device,
                    specificObject
                )
            }
        }
    }

    //Ok, then we need to subscribe to the state for the new activating connection

    //Show some throbbers as we go through the connecting states for the active connection


}
