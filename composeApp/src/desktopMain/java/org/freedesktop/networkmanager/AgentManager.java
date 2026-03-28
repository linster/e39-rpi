package org.freedesktop.networkmanager;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.AgentManager")
public interface AgentManager extends DBusInterface {

    void Register(String identifier);

    void RegisterWithCapabilities(String identifier, UInt32 capabilities);

    void Unregister();

}
