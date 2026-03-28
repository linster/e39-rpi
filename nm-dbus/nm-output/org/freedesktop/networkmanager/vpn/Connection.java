package org.freedesktop.networkmanager.vpn;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.VPN.Connection")
@DBusProperty({name=VpnState, type=UInt32, access=Access.READ})
@DBusProperty({name=Banner, type=String, access=Access.READ})
public interface Connection extends DBusInterface {

    public static class VpnStateChanged extends DBusSignal {

        private final UInt32 state;
        private final UInt32 reason;

        public VpnStateChanged(String path, UInt32 state, UInt32 reason) throws DBusException {
            super(path, state, reason);
            this.state = state;
            this.reason = reason;
        }

        public UInt32 getState() {
            return state;
        }

        public UInt32 getReason() {
            return reason;
        }

    }

}
