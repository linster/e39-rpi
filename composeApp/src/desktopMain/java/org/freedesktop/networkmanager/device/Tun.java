package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Tun")
@DBusProperty(name = "Owner", type = Long.class, access = Access.READ)
@DBusProperty(name = "Group", type = Long.class, access = Access.READ)
@DBusProperty(name = "Mode", type = String.class, access = Access.READ)
@DBusProperty(name = "NoPi", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "VnetHdr", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "MultiQueue", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "HwAddress", type = String.class, access = Access.READ)
public interface Tun extends DBusInterface {

}
