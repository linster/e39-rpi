package org.freedesktop.networkmanager.device;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Wireless")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=PermHwAddress, type=String, access=Access.READ})
@DBusProperty({name=Mode, type=UInt32, access=Access.READ})
@DBusProperty({name=Bitrate, type=UInt32, access=Access.READ})
@DBusProperty({name=AccessPoints, type=Wireless.PropertyAccessPointsType, access=Access.READ})
@DBusProperty({name=ActiveAccessPoint, type=DBusPath, access=Access.READ})
@DBusProperty({name=WirelessCapabilities, type=UInt32, access=Access.READ})
@DBusProperty({name=LastScan, type=Long, access=Access.READ})
public interface Wireless extends DBusInterface {

    List<DBusPath> GetAccessPoints();

    List<DBusPath> GetAllAccessPoints();

    void RequestScan(Map<String, Variant<?>> options);

    public static interface PropertyAccessPointsType extends TypeRef<List<DBusPath>> {

    }

    public static class AccessPointAdded extends DBusSignal {

        private final DBusPath accessPoint;

        public AccessPointAdded(String path, DBusPath accessPoint) throws DBusException {
            super(path, accessPoint);
            this.accessPoint = accessPoint;
        }

        public DBusPath getAccessPoint() {
            return accessPoint;
        }

    }

    public static class AccessPointRemoved extends DBusSignal {

        private final DBusPath accessPoint;

        public AccessPointRemoved(String path, DBusPath accessPoint) throws DBusException {
            super(path, accessPoint);
            this.accessPoint = accessPoint;
        }

        public DBusPath getAccessPoint() {
            return accessPoint;
        }

    }

}
