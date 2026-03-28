package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Tun")
@DBusProperty({name=Owner, type=Long, access=Access.READ})
@DBusProperty({name=Group, type=Long, access=Access.READ})
@DBusProperty({name=Mode, type=String, access=Access.READ})
@DBusProperty({name=NoPi, type=Boolean, access=Access.READ})
@DBusProperty({name=VnetHdr, type=Boolean, access=Access.READ})
@DBusProperty({name=MultiQueue, type=Boolean, access=Access.READ})
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
public interface Tun extends DBusInterface {

}
