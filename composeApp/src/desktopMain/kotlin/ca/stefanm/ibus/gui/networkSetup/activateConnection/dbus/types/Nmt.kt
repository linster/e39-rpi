package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

import org.freedesktop.networkmanager.AccessPoint
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.connection.Active
import org.freedesktop.networkmanager.settings.Connection

//Types from Nmtui nmt-connect-connection-list
object Nmt {

    data class NmtConnectDevice(
        val name : String,
        val device : Device,

        val sortOrder : Int? = null,
        val connections : List<NmtConnectConnection>? = null
    )

    data class NmtConnectConnection(
        val name : String? = null,
        val ssid : String? = null,

        val conn : Connection? = null,
        val ap : AccessPoint? = null,
        val device: Device? = null,
        //Active connection
        val active : Active? = null,
    )

}