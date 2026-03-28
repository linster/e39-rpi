package org.freedesktop.networkmanager.device;

import java.util.List;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
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
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.WiMax")
@DBusProperty({name=Nsps, type=WiMax.PropertyNspsType, access=Access.READ})
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=CenterFrequency, type=UInt32, access=Access.READ})
@DBusProperty({name=Rssi, type=Integer, access=Access.READ})
@DBusProperty({name=Cinr, type=Integer, access=Access.READ})
@DBusProperty({name=TxPower, type=Integer, access=Access.READ})
@DBusProperty({name=Bsid, type=String, access=Access.READ})
@DBusProperty({name=ActiveNsp, type=DBusPath, access=Access.READ})
public interface WiMax extends DBusInterface {

    List<DBusPath> GetNspList();

    public static interface PropertyNspsType extends TypeRef<List<DBusPath>> {

    }

    public static class NspAdded extends DBusSignal {

        private final DBusPath nsp;

        public NspAdded(String path, DBusPath nsp) throws DBusException {
            super(path, nsp);
            this.nsp = nsp;
        }

        public DBusPath getNsp() {
            return nsp;
        }

    }

    public static class NspRemoved extends DBusSignal {

        private final DBusPath nsp;

        public NspRemoved(String path, DBusPath nsp) throws DBusException {
            super(path, nsp);
            this.nsp = nsp;
        }

        public DBusPath getNsp() {
            return nsp;
        }

    }

}
