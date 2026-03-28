package org.freedesktop.networkmanager;

import java.util.List;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Checkpoint")
@DBusProperty(name = "Devices", type = Checkpoint.PropertyDevicesType.class, access = Access.READ)
@DBusProperty(name = "Created", type = Long.class, access = Access.READ)
@DBusProperty(name = "RollbackTimeout", type = UInt32.class, access = Access.READ)
public interface Checkpoint extends DBusInterface {

    public static interface PropertyDevicesType extends TypeRef<List<DBusPath>> {

    }

}
