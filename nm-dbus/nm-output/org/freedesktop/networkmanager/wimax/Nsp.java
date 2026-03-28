package org.freedesktop.networkmanager.wimax;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.WiMax.Nsp")
@DBusProperty({name=Name, type=String, access=Access.READ})
@DBusProperty({name=SignalQuality, type=UInt32, access=Access.READ})
@DBusProperty({name=NetworkType, type=UInt32, access=Access.READ})
public interface Nsp extends DBusInterface {

}
