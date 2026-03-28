package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Vxlan")
@DBusProperty({name=Parent, type=DBusPath, access=Access.READ})
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Id, type=UInt32, access=Access.READ})
@DBusProperty({name=Group, type=String, access=Access.READ})
@DBusProperty({name=Local, type=String, access=Access.READ})
@DBusProperty({name=Tos, type=Byte, access=Access.READ})
@DBusProperty({name=Ttl, type=Byte, access=Access.READ})
@DBusProperty({name=Learning, type=Boolean, access=Access.READ})
@DBusProperty({name=Ageing, type=UInt32, access=Access.READ})
@DBusProperty({name=Limit, type=UInt32, access=Access.READ})
@DBusProperty({name=DstPort, type=UInt16, access=Access.READ})
@DBusProperty({name=SrcPortMin, type=UInt16, access=Access.READ})
@DBusProperty({name=SrcPortMax, type=UInt16, access=Access.READ})
@DBusProperty({name=Proxy, type=Boolean, access=Access.READ})
@DBusProperty({name=Rsc, type=Boolean, access=Access.READ})
@DBusProperty({name=L2miss, type=Boolean, access=Access.READ})
@DBusProperty({name=L3miss, type=Boolean, access=Access.READ})
public interface Vxlan extends DBusInterface {

}
