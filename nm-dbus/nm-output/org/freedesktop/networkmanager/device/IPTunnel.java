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
@DBusProperty({name=Mode, type=UInt32, access=Access.READ})
@DBusProperty({name=Parent, type=DBusPath, access=Access.READ})
@DBusProperty({name=Local, type=String, access=Access.READ})
@DBusProperty({name=Remote, type=String, access=Access.READ})
@DBusProperty({name=Ttl, type=Byte, access=Access.READ})
@DBusProperty({name=Tos, type=Byte, access=Access.READ})
@DBusProperty({name=PathMtuDiscovery, type=Boolean, access=Access.READ})
@DBusProperty({name=InputKey, type=String, access=Access.READ})
@DBusProperty({name=OutputKey, type=String, access=Access.READ})
@DBusProperty({name=EncapsulationLimit, type=Byte, access=Access.READ})
@DBusProperty({name=FlowLabel, type=UInt32, access=Access.READ})
@DBusProperty({name=FwMark, type=UInt32, access=Access.READ})
@DBusProperty({name=Flags, type=UInt32, access=Access.READ})
public interface IPTunnel extends DBusInterface {

}
