package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Hsr")
@DBusProperty({name=Port1, type=DBusPath, access=Access.READ})
@DBusProperty({name=Port2, type=DBusPath, access=Access.READ})
@DBusProperty({name=SupervisionAddress, type=String, access=Access.READ})
@DBusProperty({name=MulticastSpec, type=Byte, access=Access.READ})
@DBusProperty({name=Prp, type=Boolean, access=Access.READ})
public interface Hsr extends DBusInterface {

}
