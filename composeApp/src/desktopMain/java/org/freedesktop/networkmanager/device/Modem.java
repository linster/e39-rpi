package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Modem")
@DBusProperty(name = "ModemCapabilities", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "CurrentCapabilities", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "DeviceId", type = String.class, access = Access.READ)
@DBusProperty(name = "OperatorCode", type = String.class, access = Access.READ)
@DBusProperty(name = "Apn", type = String.class, access = Access.READ)
public interface Modem extends DBusInterface {

}
