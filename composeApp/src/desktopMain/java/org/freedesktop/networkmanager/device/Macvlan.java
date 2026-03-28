package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Macvlan")
@DBusProperty(name = "Parent", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Mode", type = String.class, access = Access.READ)
@DBusProperty(name = "NoPromisc", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Tap", type = Boolean.class, access = Access.READ)
public interface Macvlan extends DBusInterface {

}
