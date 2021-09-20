package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine

import org.freedesktop.dbus.connections.impl.DBusConnection


interface Component {
    fun onSetup()
    fun onCleanup()
}

interface ConnectionDependingComponent : Component

typealias DBusSystemConnection = DBusConnection
typealias DBusSessionConnection = DBusConnection

interface ConnectionOwningComponent : Component {
    fun getSystemBusConnection() : DBusSystemConnection
    fun getSessionBusConnection() : DBusSessionConnection
}


interface ClosableComponent {
    /** Use this to release any connections the component may have */
    fun onCleanup()
}