package org.freedesktop.networkmanager.device;

import java.util.List;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Wired")
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=PermHwAddress, type=String, access=Access.READ})
@DBusProperty({name=Speed, type=UInt32, access=Access.READ})
@DBusProperty({name=S390Subchannels, type=Wired.PropertyS390SubchannelsType, access=Access.READ})
@DBusProperty({name=Carrier, type=Boolean, access=Access.READ})
public interface Wired extends DBusInterface {

    public static interface PropertyS390SubchannelsType extends TypeRef<List<String>> {

    }

}
