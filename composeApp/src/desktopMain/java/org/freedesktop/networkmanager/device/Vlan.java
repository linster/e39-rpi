package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Vlan")
@DBusProperty(name = "HwAddress", type = String.class, access = Access.READ)
@DBusProperty(name = "Carrier", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Parent", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "VlanId", type = UInt32.class, access = Access.READ)
public interface Vlan extends DBusInterface {

}
