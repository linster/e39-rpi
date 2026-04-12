package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Bluetooth")
@DBusProperty(name = "HwAddress", type = String.class, access = Access.READ)
@DBusProperty(name = "Name", type = String.class, access = Access.READ)
@DBusProperty(name = "BtCapabilities", type = UInt32.class, access = Access.READ)
public interface Bluetooth extends DBusInterface {

    /* Added by stefan */
    @DBusBoundProperty
    String getName();

    @DBusBoundProperty
    String getHwAddress();

    @DBusBoundProperty
    UInt32 getBtCapabilities();
    /* Added by stefan */
}
