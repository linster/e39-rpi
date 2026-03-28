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
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.OvsPort")
@DBusProperty(name = "Slaves", type = OvsPort.PropertySlavesType.class, access = Access.READ)
public interface OvsPort extends DBusInterface {

    public static interface PropertySlavesType extends TypeRef<List<DBusPath>> {

    }

}
