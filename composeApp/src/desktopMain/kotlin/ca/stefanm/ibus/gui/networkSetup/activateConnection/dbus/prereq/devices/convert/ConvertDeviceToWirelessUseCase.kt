package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.convert

import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.device.Bluetooth
import org.freedesktop.networkmanager.device.Wireless
import javax.inject.Inject

class ConvertDeviceToWirelessUseCase @Inject constructor(

) {

    fun deviceToWireless(device: Device) : Wireless {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        return connection.use {
            connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                device.objectPath,
                Wireless::class.java
            )
        }
    }
}