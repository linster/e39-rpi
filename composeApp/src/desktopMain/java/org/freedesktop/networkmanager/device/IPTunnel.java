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
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.IPTunnel")
@DBusProperty(name = "Mode", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Parent", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Local", type = String.class, access = Access.READ)
@DBusProperty(name = "Remote", type = String.class, access = Access.READ)
@DBusProperty(name = "Ttl", type = Byte.class, access = Access.READ)
@DBusProperty(name = "Tos", type = Byte.class, access = Access.READ)
@DBusProperty(name = "PathMtuDiscovery", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "InputKey", type = String.class, access = Access.READ)
@DBusProperty(name = "OutputKey", type = String.class, access = Access.READ)
@DBusProperty(name = "EncapsulationLimit", type = Byte.class, access = Access.READ)
@DBusProperty(name = "FlowLabel", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "FwMark", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Flags", type = UInt32.class, access = Access.READ)
public interface IPTunnel extends DBusInterface {

}
