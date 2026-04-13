package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class ConnectNmtDeviceConnectionUseCase @Inject constructor(
    private val logger: Logger
) {

    fun connect(conn : Nmt.NmtConnectConnection) {
        //Might have to listen to StateChanged signal to see if we're done
        //might also have to make this a suspend fun
    }
}
