package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

// All of this is loosely based on
//nmt_connect_connection_list_rebuild()
// in nmt-connect-connection-list.c in nmtui.

//It looks like NmDevice gets decorated
//with a connection list. The sort-order is
//added when
//data class NmtConnectDevice(
//    val name : String,
//    val device : Device,
//
//    val sortOrder : Int,
//
//    //https://networkmanager.dev/docs/libnm/latest/NMClient.html#nm-client-get-connections ??
//    val connections : List<Connection>
//)
//class NmDeviceGetter @Inject constructor() {
//}