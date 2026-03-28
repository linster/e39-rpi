package org.freedesktop.networkmanager;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device")
@DBusProperty({name=Udi, type=String, access=Access.READ})
@DBusProperty({name=Path, type=String, access=Access.READ})
@DBusProperty({name=Interface, type=String, access=Access.READ})
@DBusProperty({name=IpInterface, type=String, access=Access.READ})
@DBusProperty({name=Driver, type=String, access=Access.READ})
@DBusProperty({name=DriverVersion, type=String, access=Access.READ})
@DBusProperty({name=FirmwareVersion, type=String, access=Access.READ})
@DBusProperty({name=Capabilities, type=UInt32, access=Access.READ})
@DBusProperty({name=Ip4Address, type=UInt32, access=Access.READ})
@DBusProperty({name=State, type=UInt32, access=Access.READ})
@DBusProperty({name=StateReason, type=PropertyStateReasonStruct, access=Access.READ})
@DBusProperty({name=ActiveConnection, type=DBusPath, access=Access.READ})
@DBusProperty({name=Ip4Config, type=DBusPath, access=Access.READ})
@DBusProperty({name=Dhcp4Config, type=DBusPath, access=Access.READ})
@DBusProperty({name=Ip6Config, type=DBusPath, access=Access.READ})
@DBusProperty({name=Dhcp6Config, type=DBusPath, access=Access.READ})
@DBusProperty({name=Managed, type=Boolean, access=Access.READ_WRITE})
@DBusProperty({name=Autoconnect, type=Boolean, access=Access.READ_WRITE})
@DBusProperty({name=FirmwareMissing, type=Boolean, access=Access.READ})
@DBusProperty({name=NmPluginMissing, type=Boolean, access=Access.READ})
@DBusProperty({name=DeviceType, type=UInt32, access=Access.READ})
@DBusProperty({name=AvailableConnections, type=Device.PropertyAvailableConnectionsType, access=Access.READ})
@DBusProperty({name=PhysicalPortId, type=String, access=Access.READ})
@DBusProperty({name=Mtu, type=UInt32, access=Access.READ})
@DBusProperty({name=Metered, type=UInt32, access=Access.READ})
@DBusProperty({name=LldpNeighbors, type=Device.PropertyLldpNeighborsType, access=Access.READ})
@DBusProperty({name=Real, type=Boolean, access=Access.READ})
@DBusProperty({name=Ip4Connectivity, type=UInt32, access=Access.READ})
@DBusProperty({name=Ip6Connectivity, type=UInt32, access=Access.READ})
@DBusProperty({name=InterfaceFlags, type=UInt32, access=Access.READ})
@DBusProperty({name=HwAddress, type=String, access=Access.READ})
@DBusProperty({name=Ports, type=Device.PropertyPortsType, access=Access.READ})
public interface Device extends DBusInterface {

    void Reapply(Map<String, Map<String, Variant<?>>> connection, UInt64 versionId, UInt32 flags);

    GetAppliedConnectionTuple<Map<String, Map<String, Variant<?>>>, UInt64> GetAppliedConnection(UInt32 flags);

    void Disconnect();

    void Delete();

    void SetManaged(UInt32 managed, UInt32 flags);

    public static interface PropertyAvailableConnectionsType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyLldpNeighborsType extends TypeRef<List<Map<String, Variant>>> {

    }

    public static interface PropertyPortsType extends TypeRef<List<DBusPath>> {

    }

    public static class StateChanged extends DBusSignal {

        private final UInt32 newState;
        private final UInt32 oldState;
        private final UInt32 reason;

        public StateChanged(String path, UInt32 newState, UInt32 oldState, UInt32 reason) throws DBusException {
            super(path, newState, oldState, reason);
            this.newState = newState;
            this.oldState = oldState;
            this.reason = reason;
        }

        public UInt32 getNewState() {
            return newState;
        }

        public UInt32 getOldState() {
            return oldState;
        }

        public UInt32 getReason() {
            return reason;
        }

    }

}
