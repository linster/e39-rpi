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
@DBusInterfaceName("org.freedesktop.NetworkManager.DHCP6Config")
@DBusProperty({name=Options, type=DHCP6Config.PropertyOptionsType, access=Access.READ})
public interface DHCP6Config extends DBusInterface {

    public static interface PropertyOptionsType extends TypeRef<Map<String, Variant>> {

    }

}
