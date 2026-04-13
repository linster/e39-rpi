package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class DisconnectNmtDeviceConnectionUseCase @Inject constructor(
    private val logger: Logger
) {

    fun disconnect(conn : Nmt.NmtConnectConnection) {

    }
}