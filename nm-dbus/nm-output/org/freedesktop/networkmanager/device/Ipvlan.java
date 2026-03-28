package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Ipvlan")
@DBusProperty({name=Parent, type=DBusPath, access=Access.READ})
@DBusProperty({name=Vepa, type=Boolean, access=Access.READ})
@DBusProperty({name=Mode, type=String, access=Access.READ})
@DBusProperty({name=Private, type=Boolean, access=Access.READ})
public interface Ipvlan extends DBusInterface {

}
