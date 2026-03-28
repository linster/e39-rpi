package org.freedesktop.networkmanager;

import java.util.Map;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.DHCP4Config")
@DBusProperty({name=Options, type=DHCP4Config.PropertyOptionsType, access=Access.READ})
public interface DHCP4Config extends DBusInterface {

    public static interface PropertyOptionsType extends TypeRef<Map<String, Variant>> {

    }

}
