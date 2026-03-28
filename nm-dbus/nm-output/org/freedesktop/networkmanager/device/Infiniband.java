package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Infiniband")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Carrier, type=Boolean, access=Access.READ})
public interface Infiniband extends DBusInterface {

}
