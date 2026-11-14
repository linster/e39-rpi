package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMActiveConnectionState
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.WifiSecurity
import ca.stefanm.ibus.gui.networkSetup.editConnection.dbus.SavedConnectionsUseCase
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBus
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.types.Variant
import org.freedesktop.networkmanager.connection.Active
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ConnectNmtDeviceConnectionUseCase @Inject constructor(
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val savedConnectionsUseCase: SavedConnectionsUseCase
) {

    companion object {
        const val TAG = "ConnectNmtDeviceConnectionUseCase"

        private val ACTIVATION_TIMEOUT = 45.seconds
        private val NO_SPECIFIC_OBJECT = DBusPath("/")

        //NMActiveConnectionStateReason
        fun reasonText(reason : Int?) : String? = when (reason) {
            2 -> "Disconnected by the user."
            3 -> "The device was disconnected."
            4 -> "The VPN service stopped."
            5 -> "The IP configuration was invalid."
            6 -> "The connection timed out."
            7 -> "The VPN service took too long to start."
            8 -> "The VPN service failed to start."
            9 -> "No password or secrets were provided."
            10 -> "Login failed."
            11 -> "The connection was deleted."
            12 -> "A connection it depends on failed."
            13 -> "Could not create the device."
            14 -> "The device was removed."
            else -> null
        }
    }

    sealed interface ConnectOutcome {
        data object Connected : ConnectOutcome
        data object Failed : ConnectOutcome
        //The wifi network has no saved profile and needs a password before we can add one.
        data class NeedsSecret(val security : WifiSecurity) : ConnectOutcome
    }

    suspend fun connect(conn : Nmt.NmtConnectConnection) : ConnectOutcome {
        //https://networkmanager.dev/docs/api/latest/gdbus-org.freedesktop.NetworkManager.html#gdbus-method-org-freedesktop-NetworkManager.ActivateConnection
        logger.i(TAG, "Starting connection flow for $conn")
        return runConnection(conn.ssid ?: conn.name ?: "connection") {
            if (conn.deviceIsWifi == true) {
                activateWifi(conn, secret = null)
            } else if (conn.deviceIsVpn == true) {
                activateVpn(conn)
            } else {
                activateRegular(conn)
            }
        }
    }

    //Used after the user has typed in a password for a wifi network, either for a new
    //network or to replace the password of a saved one.
    suspend fun connectWifiWithSecret(conn : Nmt.NmtConnectConnection, secret : String) : ConnectOutcome {
        logger.i(TAG, "Connecting to wifi ${conn.ssid} with a new password")
        return runConnection(conn.ssid ?: "wifi network") {
            activateWifi(conn, secret)
        }
    }

    private suspend fun runConnection(
        name : String,
        block : suspend () -> ConnectOutcome
    ) : ConnectOutcome = withContext(Dispatchers.IO) {
        showProgress(name, "Starting connection")
        try {
            block()
        } catch (e : Throwable) {
            logger.e(TAG, "Failed to activate connection $name", e)
            showError("Couldn't connect to $name", e.message ?: e.toString())
            ConnectOutcome.Failed
        }
    }

    private suspend fun activateRegular(conn : Nmt.NmtConnectConnection) : ConnectOutcome {
        val name = conn.name ?: "connection"
        val active = activateConnection(
            device = DBusPath(conn.device!!.objectPath),
            connection = DBusPath(conn.conn!!.objectPath),
            specificObject = NO_SPECIFIC_OBJECT
        )
        return monitorActivation(name, active, failureHint = null)
    }

    private suspend fun activateVpn(conn : Nmt.NmtConnectConnection) : ConnectOutcome {
        val name = conn.name ?: "VPN"
        //VPNs aren't tied to a device, NetworkManager picks the one with the default route.
        val active = activateConnection(
            device = NO_SPECIFIC_OBJECT,
            connection = DBusPath(conn.conn!!.objectPath),
            specificObject = NO_SPECIFIC_OBJECT
        )
        return monitorActivation(name, active, failureHint = null)
    }

    private suspend fun activateWifi(conn : Nmt.NmtConnectConnection, secret : String?) : ConnectOutcome {
        val ssid = conn.ssid ?: error("Wifi network has no SSID")
        val device = DBusPath(conn.device?.objectPath ?: error("Wifi network has no device"))
        val ap = conn.ap ?: error("Wifi network has no access point")
        val apPath = DBusPath(ap.objectPath)

        val passwordHint = "If the password is wrong, choose Change Password for the network."

        //Wifi entries in the list are access points, so find the profile NetworkManager saved for the SSID.
        val saved = savedConnectionsUseCase.findSavedWifiConnection(ssid)
        if (saved != null) {
            if (secret != null) {
                savedConnectionsUseCase.setWifiPassword(saved.path, secret)
            }
            val active = activateConnection(
                connection = saved.path,
                device = device,
                specificObject = apPath
            )
            return monitorActivation(ssid, active, failureHint = passwordHint.takeIf { saved.hasWifiPassword })
        }

        val security = WifiSecurity.fromAccessPoint(ap)
        if (!security.isSupported) {
            showError(
                "Can't connect to $ssid",
                "${security.label} networks aren't supported here. Set it up with nmtui instead."
            )
            return ConnectOutcome.Failed
        }

        if (security.needsPassword && secret == null) {
            modalMenuService.closeModalMenu()
            return ConnectOutcome.NeedsSecret(security)
        }

        val active = addAndActivateWifi(ssid, security, secret, device, apPath)
        return monitorActivation(ssid, active, failureHint = passwordHint.takeIf { security.needsPassword })
    }

    private fun newWifiSettings(
        ssid : String,
        security : WifiSecurity,
        secret : String?
    ) : Map<String, Map<String, Variant<*>>> {
        //NetworkManager fills in the rest of the profile (uuid, ipv4, ipv6...) itself.
        return buildMap {
            put(SavedConnectionsUseCase.SETTING_CONNECTION, mapOf(
                "id" to Variant(ssid),
                "type" to Variant(SavedConnectionsUseCase.TYPE_WIFI)
            ))
            put(SavedConnectionsUseCase.SETTING_WIFI, mapOf(
                "ssid" to Variant(ssid.toByteArray()),
                "mode" to Variant("infrastructure")
            ))
            if (security.keyMgmt != null) {
                put(SavedConnectionsUseCase.SETTING_WIFI_SECURITY, buildMap {
                    put("key-mgmt", Variant(security.keyMgmt))
                    if (secret != null) {
                        put("psk", Variant(secret))
                    }
                })
            }
        }
    }

    private fun addAndActivateWifi(
        ssid : String,
        security : WifiSecurity,
        secret : String?,
        device : DBusPath,
        accessPoint : DBusPath
    ) : DBusPath {
        return DBusConnectionBuilder.forSystemBus().build().use {
            val nmClient = it.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )
            nmClient.AddAndActivateConnection(
                newWifiSettings(ssid, security, secret),
                device,
                accessPoint
            ).activeConnection
        }
    }

    private fun activateConnection(
        connection : DBusPath,
        device : DBusPath,
        specificObject : DBusPath
    ) : DBusPath {
        return DBusConnectionBuilder.forSystemBus().build().use {
            val nmClient = it.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )
            nmClient.ActivateConnection(
                connection,
                device,
                specificObject
            )
        }
    }

    //Show the activating states for the new active connection until it's either up or has failed.
    private suspend fun monitorActivation(
        name : String,
        activeConnection : DBusPath,
        failureHint : String?
    ) : ConnectOutcome {
        showProgress(name, "Activating")

        val finalState = withTimeoutOrNull(ACTIVATION_TIMEOUT) {
            watchActiveConnectionState(activeConnection)
                .onEach { (state, _) -> showProgress(name, stateText(state)) }
                .first { (state, _) ->
                    state == NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED
                            || state == NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATED
                }
        }

        return when (finalState?.first) {
            null -> {
                showError("Error connecting to $name", "No status update received")
                ConnectOutcome.Failed
            }
            NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED -> {
                modalMenuService.closeModalMenu()
                notificationHub.postNotificationBackground(Notification(
                    topText = "Connected",
                    contentText = name
                ))
                ConnectOutcome.Connected
            }
            else -> {
                showError(
                    "Couldn't connect to $name",
                    listOfNotNull(reasonText(finalState.second), failureHint).joinToString(" ")
                )
                ConnectOutcome.Failed
            }
        }
    }

    private fun watchActiveConnectionState(
        activeConnection : DBusPath
    ) : Flow<Pair<NMActiveConnectionState.State, Int?>> = callbackFlow {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        connection.connect()

        val active = connection.getRemoteObject(
            "org.freedesktop.NetworkManager",
            activeConnection.path,
            Active::class.java
        )

        val handler = object : DBusSigHandler<Active.StateChanged> {
            override fun handle(_signal: Active.StateChanged?) {
                if (_signal != null) {
                    trySend(NMActiveConnectionState.fromInt(_signal.state) to _signal.reason.toInt())
                }
            }
        }

        connection.addSigHandler(
            Active.StateChanged::class.java,
            connection.getRemoteObject(
                "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
            )!!.GetNameOwner("org.freedesktop.NetworkManager")!!,
            active,
            handler
        )

        //Read the state after subscribing so a change in between isn't missed.
        //If the active connection is already gone, it failed before we could look at it.
        val initialState = try {
            NMActiveConnectionState.fromInt(active.state)
        } catch (e : Throwable) {
            logger.w(TAG, "Active connection $activeConnection disappeared: ${e.message}")
            NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATED
        }
        trySend(initialState to null)

        awaitClose {
            connection.removeSigHandler(
                Active.StateChanged::class.java,
                handler
            )
            connection.close()
        }
    }

    private fun stateText(state : NMActiveConnectionState.State) : String = when (state) {
        NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_UNKNOWN -> "Unknown"
        NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATING -> "Activating"
        NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED -> "Activated"
        NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATING -> "Deactivating"
        NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_DEACTIVATED -> "Deactivated"
    }

    private fun showProgress(name : String, status : String) {
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.NONE,
            throbber = true,
            headerText = "Connecting to $name",
            bodyText = status,
            autoCloseTimeout = null,
            isCancellable = false,
        )
    }

    private fun showError(header : String, body : String) {
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.ALERT_TRIANGLE,
            throbber = false,
            headerText = header,
            bodyText = body,
            autoCloseTimeout = 15.seconds,
            isCancellable = true,
            onCancel = {
                modalMenuService.closeModalMenu()
            }
        )
    }
}
