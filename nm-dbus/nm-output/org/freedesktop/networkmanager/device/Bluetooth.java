package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Bluetooth")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Name, type=String, access=Access.READ})
@DBusProperty({name=BtCapabilities, type=UInt32, access=Access.READ})
public interface Bluetooth extends DBusInterface {

}
