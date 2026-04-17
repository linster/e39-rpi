package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

import org.freedesktop.dbus.types.UInt32

object NMActiveConnectionState {

    /**
     * NMActiveConnectionState:
     * @NM_ACTIVE_CONNECTION_STATE_UNKNOWN: the state of the connection is unknown
     * @NM_ACTIVE_CONNECTION_STATE_ACTIVATING: a network connection is being prepared
     * @NM_ACTIVE_CONNECTION_STATE_ACTIVATED: there is a connection to the network
     * @NM_ACTIVE_CONNECTION_STATE_DEACTIVATING: the network connection is being
     *   torn down and cleaned up
     * @NM_ACTIVE_CONNECTION_STATE_DEACTIVATED: the network connection is disconnected
     *   and will be removed
     *
     * #NMActiveConnectionState values indicate the state of a connection to a
     * specific network while it is starting, connected, or disconnecting from that
     * network.
     **/
    enum class State(val raw : Int) {
        NM_ACTIVE_CONNECTION_STATE_UNKNOWN      (raw = 0),
        NM_ACTIVE_CONNECTION_STATE_ACTIVATING   (raw = 1),
        NM_ACTIVE_CONNECTION_STATE_ACTIVATED    (raw = 2),
        NM_ACTIVE_CONNECTION_STATE_DEACTIVATING (raw = 3),
        NM_ACTIVE_CONNECTION_STATE_DEACTIVATED  (raw = 4),
    }

    fun fromInt(raw : UInt32) : State {
        return State.values().find { it.raw == raw.toInt() } ?: State.NM_ACTIVE_CONNECTION_STATE_UNKNOWN
    }
}