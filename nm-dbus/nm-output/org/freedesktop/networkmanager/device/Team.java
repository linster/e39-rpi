package org.freedesktop.networkmanager.device;

import java.util.List;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Team")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Carrier, type=Boolean, access=Access.READ})
@DBusProperty({name=Slaves, type=Team.PropertySlavesType, access=Access.READ})
@DBusProperty({name=Config, type=String, access=Access.READ})
public interface Team extends DBusInterface {

    public static interface PropertySlavesType extends TypeRef<List<DBusPath>> {

    }

}
