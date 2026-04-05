package org.freedesktop.networkmanager;

import java.util.List;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.AccessPoint")
@DBusProperty(name = "Flags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "WpaFlags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "RsnFlags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Ssid", type = AccessPoint.PropertySsidType.class, access = Access.READ)
@DBusProperty(name = "Frequency", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "HwAddress", type = String.class, access = Access.READ)
@DBusProperty(name = "Mode", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "MaxBitrate", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Bandwidth", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Strength", type = Byte.class, access = Access.READ)
@DBusProperty(name = "LastSeen", type = Integer.class, access = Access.READ)
public interface AccessPoint extends DBusInterface {

    /* Added by Stefan */
    @DBusBoundProperty
    List<Byte> getSsid();
    /* Added by Stefan */

    public static interface PropertySsidType extends TypeRef<List<Byte>> {

    }

}
