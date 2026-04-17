package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.DevicePath
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

class GetDeviceFromDevicePathUseCase @Inject constructor(

){

    fun getDevice(path : DevicePath) : Device {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        return connection.use {
            connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                path,
                Device::class.java
            )
        }
    }
}