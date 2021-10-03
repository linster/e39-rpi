package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine

import com.github.hypfvieh.bluetooth.DeviceManager
import org.freedesktop.dbus.connections.impl.DBusConnection


interface Component {
    fun onSetup()
    fun onCleanup()
}


typealias DBusSystemConnection = DBusConnection
typealias DBusSessionConnection = DBusConnection

interface DBusConnectionOwningComponent : Component {
    fun getSystemBusConnection() : DBusSystemConnection
    fun getSessionBusConnection() : DBusSessionConnection
    fun getDeviceManager() : DeviceManager
}

interface DBusConnectionDependingComponent : Component