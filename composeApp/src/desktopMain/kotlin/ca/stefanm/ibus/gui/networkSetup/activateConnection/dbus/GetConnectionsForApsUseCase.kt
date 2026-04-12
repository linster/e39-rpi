package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.networkmanager.AccessPoint
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.device.Wireless
import org.freedesktop.networkmanager.settings.Connection
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.TimeSource

class GetConnectionsForApsUseCase @Inject constructor(
    private val logger: Logger
) {

    companion object {
        const val TAG = "GetConnectionsForApsUseCase"
    }
    data class AccessPointWithSsid(
        val accessPoint: AccessPoint,
        val ssid: String,
        val isActive : Boolean
    )

    //Invalidate whatever is returned and recompute when there's more wifi
    //devices that come in...
    fun fillInNmtConnectConnectionForWirelessOnly(
        upstream : Flow<Map<Device, List<Nmt.NmtConnectConnection>>>
    ) : Flow<Map<Device, List<Nmt.NmtConnectConnection>>> {
        return upstream.flatMapLatest { upstreamMap ->
            callbackFlow {
                val connection = DBusConnectionBuilder.forSystemBus().build()
                connection.connect()

                val data = fillInNmtConnectConnectionForWirelessOnly(upstreamMap)
                send(data)

                val wirelessDevices = data.keys.filter {
                    it.deviceType.toNMDeviceType() == NMDeviceType.NM_DEVICE_TYPE_WIFI
                }.map { convertDeviceToWireless(device = it) }

                val addedHandlers = mutableListOf<DBusSigHandler<Wireless.AccessPointAdded>>()
                val removedHandlers = mutableListOf<DBusSigHandler<Wireless.AccessPointRemoved>>()

                wirelessDevices.forEach { device ->
                    val addedHandler = getNewApAddedSigHandler(device) {
                        if (upstreamMap.isNotEmpty()) {
                            trySend(fillInNmtConnectConnectionForWirelessOnly(upstreamMap))
                        }
                    }
                    addedHandlers.addLast(addedHandler)

                    val removedHandler = getNewApRemovedSigHandler(device) {
                        if (upstreamMap.isNotEmpty()) {
                            trySend(fillInNmtConnectConnectionForWirelessOnly(upstreamMap))
                        }
                    }
                    removedHandlers.addLast(removedHandler)

                    connection.addSigHandler(
                        Wireless.AccessPointAdded::class.java,
                        device,
                        addedHandler
                    )
                    connection.addSigHandler(
                        Wireless.AccessPointRemoved::class.java,
                        device,
                        removedHandler
                    )
                }


                awaitClose {
                    addedHandlers.forEach {
                        connection.removeSigHandler(
                            Wireless.AccessPointAdded::class.java,
                            it
                        )
                    }

                    removedHandlers.forEach {
                        connection.removeSigHandler(
                            Wireless.AccessPointRemoved::class.java,
                            it
                        )
                    }

                    connection.close()
                }
            }.catch {
                logger.e(TAG, "Error in usecase", it)
                emit(upstreamMap)
            }
        }
    }

    @VisibleForTesting
    fun fillInNmtConnectConnectionForWirelessOnly(
        devices : Map<Device, List<Nmt.NmtConnectConnection>>
    ) : Map<Device, List<Nmt.NmtConnectConnection>> {
        val (wifi, others) = devices
            .entries
            .partition { it.key.deviceType.toNMDeviceType() == NMDeviceType.NM_DEVICE_TYPE_WIFI }


        val builtWifi = wifi.map {
            val device = it.key
            val wireless = convertDeviceToWireless(device)
            device to getConnectionsForDevice(wireless).map {
                Nmt.NmtConnectConnection(
                    ap = it.accessPoint,
                    ssid = it.ssid,
                    deviceIsWifi = true,
                    device = device,
                    apIsactive = it.isActive
                )
            }
        }

        return buildMap {
            putAll(builtWifi)
            putAll(others.map { it.key to it.value })
        }
    }

    fun getConnectionsForAps(
        wirelessDevices : List<Wireless>
    ) : Map<Wireless, List<AccessPointWithSsid>> {
        return wirelessDevices.map { wireless ->
            wireless to getConnectionsForDevice(wireless)
        }.associate {
            it.first to it.second
        }
    }

    fun getConnectionsForDevice(
        wireless: Wireless
    ) : List<AccessPointWithSsid> {
        val accessPoints = wireless.GetAccessPoints().toList().map { getAccessPointFromPath(it) }
        return accessPoints.map { ap ->
            AccessPointWithSsid(
                accessPoint = ap,
                ssid = String(ap.ssid.toByteArray()),
                isActive = DBusPath(ap.objectPath) == wireless.activeAccessPoint,
            )
        }
    }

    fun getAccessPointFromPath(path : DBusPath) : AccessPoint {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        connection.connect()
        return connection.use {
            it.getRemoteObject(
                "org.freedesktop.NetworkManager",
                path,
                AccessPoint::class.java
            )
        }
    }

    fun convertDeviceToWireless(device: Device) : Wireless {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        connection.connect()
        return connection.use {
            it.getRemoteObject(
                "org.freedesktop.NetworkManager",
                DBusPath(device.objectPath),
                Wireless::class.java
            )
        }
    }

    fun getNewApAddedSigHandler(wireless: Wireless, onEvent : () -> Unit) : DBusSigHandler<Wireless.AccessPointAdded>{
        return object : DBusSigHandler<Wireless.AccessPointAdded> {
            override fun handle(_signal: Wireless.AccessPointAdded?) {
                logger.d(TAG, "Access Point added for ${wireless.objectPath}")
                onEvent()
            }
        }
    }

    fun getNewApRemovedSigHandler(wireless: Wireless, onEvent: () -> Unit) : DBusSigHandler<Wireless.AccessPointRemoved>{
        return object : DBusSigHandler<Wireless.AccessPointRemoved> {
            override fun handle(_signal: Wireless.AccessPointRemoved?) {
                logger.d(TAG, "Access Point removed for ${wireless.objectPath}")
                onEvent()
            }
        }
    }
}