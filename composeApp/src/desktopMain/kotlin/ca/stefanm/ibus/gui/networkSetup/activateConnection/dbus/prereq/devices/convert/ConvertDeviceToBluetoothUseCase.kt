package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.convert

import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.device.Bluetooth
import javax.inject.Inject

class ConvertDeviceToBluetoothUseCase @Inject constructor(){

    fun deviceToBluetooth(device: Device) : Bluetooth {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        return connection.use {
            connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                device.objectPath,
                Bluetooth::class.java
            )
        }
    }
}