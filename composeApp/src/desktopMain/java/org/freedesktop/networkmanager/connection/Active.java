package org.freedesktop.networkmanager.connection;

import java.util.List;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
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
@DBusInterfaceName("org.freedesktop.NetworkManager.Connection.Active")
@DBusProperty(name = "Connection", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "SpecificObject", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Id", type = String.class, access = Access.READ)
@DBusProperty(name = "Uuid", type = String.class, access = Access.READ)
@DBusProperty(name = "Type", type = String.class, access = Access.READ)
@DBusProperty(name = "Devices", type = Active.PropertyDevicesType.class, access = Access.READ)
@DBusProperty(name = "State", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "StateFlags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Default", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Ip4Config", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Dhcp4Config", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Default6", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Ip6Config", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Dhcp6Config", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Vpn", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Controller", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Master", type = DBusPath.class, access = Access.READ)
public interface Active extends DBusInterface {

    /* Added by Stefan */
    @DBusBoundProperty
    List<DBusPath> getDevices();
    /* Added by Stefan */

    public static interface PropertyDevicesType extends TypeRef<List<DBusPath>> {

    }

    public static class StateChanged extends DBusSignal {

        private final UInt32 state;
        private final UInt32 reason;

        public StateChanged(String path, UInt32 state, UInt32 reason) throws DBusException {
                super(path, state, reason);        this.state = state;
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
