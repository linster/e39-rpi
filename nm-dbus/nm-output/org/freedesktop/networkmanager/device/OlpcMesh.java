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
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.OlpcMesh")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Companion, type=DBusPath, access=Access.READ})
@DBusProperty({name=ActiveChannel, type=UInt32, access=Access.READ})
public interface OlpcMesh extends DBusInterface {

}
