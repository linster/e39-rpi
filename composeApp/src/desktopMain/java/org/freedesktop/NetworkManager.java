package org.freedesktop;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusProperty(name = "Devices", type = NetworkManager.PropertyDevicesType.class, access = Access.READ)
@DBusProperty(name = "AllDevices", type = NetworkManager.PropertyAllDevicesType.class, access = Access.READ)
@DBusProperty(name = "Checkpoints", type = NetworkManager.PropertyCheckpointsType.class, access = Access.READ)
@DBusProperty(name = "NetworkingEnabled", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "WirelessEnabled", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "WirelessHardwareEnabled", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "WwanEnabled", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "WwanHardwareEnabled", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "WimaxEnabled", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "WimaxHardwareEnabled", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "RadioFlags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "ActiveConnections", type = NetworkManager.PropertyActiveConnectionsType.class, access = Access.READ)
@DBusProperty(name = "PrimaryConnection", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "PrimaryConnectionType", type = String.class, access = Access.READ)
@DBusProperty(name = "Metered", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "ActivatingConnection", type = DBusPath.class, access = Access.READ)
@DBusProperty(name = "Startup", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Version", type = String.class, access = Access.READ)
@DBusProperty(name = "VersionInfo", type = NetworkManager.PropertyVersionInfoType.class, access = Access.READ)
@DBusProperty(name = "Capabilities", type = NetworkManager.PropertyCapabilitiesType.class, access = Access.READ)
@DBusProperty(name = "State", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Connectivity", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "ConnectivityCheckAvailable", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "ConnectivityCheckEnabled", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "ConnectivityCheckUri", type = String.class, access = Access.READ)
@DBusProperty(name = "GlobalDnsConfiguration", type = NetworkManager.PropertyGlobalDnsConfigurationType.class, access = Access.READ_WRITE)
public interface NetworkManager extends DBusInterface {

    void Reload(UInt32 flags);

    List<DBusPath> GetDevices();

    List<DBusPath> GetAllDevices();

    DBusPath GetDeviceByIpIface(String iface);

    DBusPath ActivateConnection(DBusPath connection, DBusPath device, DBusPath specificObject);

    AddAndActivateConnectionTuple<DBusPath, DBusPath> AddAndActivateConnection(Map<String, Map<String, Variant<?>>> connection, DBusPath device, DBusPath specificObject);

    AddAndActivateConnection2Tuple<DBusPath, DBusPath, Map<String, Variant<?>>> AddAndActivateConnection2(Map<String, Map<String, Variant<?>>> connection, DBusPath device, DBusPath specificObject, Map<String, Variant<?>> options);

    void DeactivateConnection(DBusPath activeConnection);

    void Sleep(boolean sleep);

    void Enable(boolean enable);

    Map<String, String> GetPermissions();

    void SetLogging(String level, String domains);

    GetLoggingTuple<String, String> GetLogging();

    UInt32 CheckConnectivity();

    UInt32 state();

    DBusPath CheckpointCreate(List<DBusPath> devices, UInt32 rollbackTimeout, UInt32 flags);

    void CheckpointDestroy(DBusPath checkpoint);

    Map<String, UInt32> CheckpointRollback(DBusPath checkpoint);

    void CheckpointAdjustRollbackTimeout(DBusPath checkpoint, UInt32 addTimeout);

    public static class CheckPermissions extends DBusSignal {

        public CheckPermissions(String path) throws DBusException {
                super(path);
        }

    }

    public static interface PropertyDevicesType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyAllDevicesType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyCheckpointsType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyActiveConnectionsType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyVersionInfoType extends TypeRef<List<UInt32>> {

    }

    public static interface PropertyCapabilitiesType extends TypeRef<List<UInt32>> {

    }

    public static class StateChanged extends DBusSignal {

        private final UInt32 state;

        public StateChanged(String path, UInt32 state) throws DBusException {
                super(path, state);        this.state = state;
        }

        public UInt32 getState() {
            return state;
        }

    }

    public static interface PropertyGlobalDnsConfigurationType extends TypeRef<Map<String, Variant>> {

    }

    public static class DeviceAdded extends DBusSignal {

        private final DBusPath devicePath;

        public DeviceAdded(String path, DBusPath devicePath) throws DBusException {
                super(path, devicePath);        this.devicePath = devicePath;
        }

        public DBusPath getDevicePath() {
            return devicePath;
        }

    }

    public static class DeviceRemoved extends DBusSignal {

        private final DBusPath devicePath;

        public DeviceRemoved(String path, DBusPath devicePath) throws DBusException {
                super(path, devicePath);        this.devicePath = devicePath;
        }

        public DBusPath getDevicePath() {
            return devicePath;
        }

    }

}
