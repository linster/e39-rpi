package org.freedesktop.networkmanager;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
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
@DBusInterfaceName("org.freedesktop.NetworkManager.Settings")
@DBusProperty(name = "Connections", type = Settings.PropertyConnectionsType.class, access = Access.READ)
@DBusProperty(name = "Hostname", type = String.class, access = Access.READ)
@DBusProperty(name = "CanModify", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "VersionId", type = UInt64.class, access = Access.READ)
public interface Settings extends DBusInterface {

    /** Added by stefan */
    @DBusBoundProperty
    String getHostname();

    @DBusBoundProperty
    List<DBusPath> getConnections();
    /** Added by stefan */

    List<DBusPath> ListConnections();

    DBusPath GetConnectionByUuid(String uuid);

    DBusPath AddConnection(Map<String, Map<String, Variant<?>>> connection);

    DBusPath AddConnectionUnsaved(Map<String, Map<String, Variant<?>>> connection);

    AddConnection2Tuple<DBusPath, Map<String, Variant<?>>> AddConnection2(Map<String, Map<String, Variant<?>>> settings, UInt32 flags, Map<String, Variant<?>> args);

    LoadConnectionsTuple<Boolean, List<String>> LoadConnections(List<String> filenames);

    boolean ReloadConnections();

    void SaveHostname(String hostname);

    public static interface PropertyConnectionsType extends TypeRef<List<DBusPath>> {

    }

    public static class NewConnection extends DBusSignal {

        private final DBusPath connection;

        public NewConnection(String path, DBusPath connection) throws DBusException {
                super(path, connection);        this.connection = connection;
        }

        public DBusPath getConnection() {
            return connection;
        }

    }

    public static class ConnectionRemoved extends DBusSignal {

        private final DBusPath connection;

        public ConnectionRemoved(String path, DBusPath connection) throws DBusException {
                super(path, connection);        this.connection = connection;
        }

        public DBusPath getConnection() {
            return connection;
        }

    }

}
