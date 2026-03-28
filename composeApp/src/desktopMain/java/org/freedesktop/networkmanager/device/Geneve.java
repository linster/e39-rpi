package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Geneve")
@DBusProperty(name = "Id", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Remote", type = String.class, access = Access.READ)
@DBusProperty(name = "Tos", type = Byte.class, access = Access.READ)
@DBusProperty(name = "Ttl", type = Integer.class, access = Access.READ)
@DBusProperty(name = "Df", type = Byte.class, access = Access.READ)
@DBusProperty(name = "DstPort", type = UInt16.class, access = Access.READ)
public interface Geneve extends DBusInterface {

}
