package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.AccessPoint
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.device.Wireless
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

class GetConnectionsForApsUseCase @Inject constructor(
    private val logger: Logger
) {

    data class AccessPointWithSsid(
        val accessPoint: AccessPoint,
        val ssid: String,
        val isActive : Boolean
    )

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
                isActive = DBusPath(ap.objectPath) == wireless.activeAccessPoint
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
}