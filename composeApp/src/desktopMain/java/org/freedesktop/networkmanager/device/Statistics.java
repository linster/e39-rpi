package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Statistics")
@DBusProperty(name = "RefreshRateMs", type = UInt32.class, access = Access.READ_WRITE)
@DBusProperty(name = "TxBytes", type = UInt64.class, access = Access.READ)
@DBusProperty(name = "RxBytes", type = UInt64.class, access = Access.READ)
public interface Statistics extends DBusInterface {

}
