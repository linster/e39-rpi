package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

import androidx.compose.runtime.Stable
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

    // This class needs to hold everything that the
    // ActivateConnection() call needs.
    // For wifi connections, one of the parameters is the object
    // path for the access point.
    @Stable
    data class NmtConnectConnection(
        val name : String? = null,

        val device: Device? = null,

        val conn : Connection? = null,

        val deviceIsWifi : Boolean? = null,
        //If it's a wifi connection,
        val ssid : String? = null,
        val ap : AccessPoint? = null,
        val apIsactive : Boolean? = null,

        //Active connection
        val hasActiveConnection : Boolean = false,
        val active : Active? = null,
    )

}