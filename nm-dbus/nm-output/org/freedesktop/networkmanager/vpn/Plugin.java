package org.freedesktop.networkmanager.vpn;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
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
@DBusInterfaceName("org.freedesktop.NetworkManager.VPN.Plugin")
@DBusProperty({name=State, type=UInt32, access=Access.READ})
public interface Plugin extends DBusInterface {

    void Connect(Map<String, Map<String, Variant<?>>> connection);

    void ConnectInteractive(Map<String, Map<String, Variant<?>>> connection, Map<String, Variant<?>> details);

    String NeedSecrets(Map<String, Map<String, Variant<?>>> settings);

    void Disconnect();

    void SetConfig(Map<String, Variant<?>> config);

    void SetIp4Config(Map<String, Variant<?>> config);

    void SetIp6Config(Map<String, Variant<?>> config);

    void SetFailure(String reason);

    void NewSecrets(Map<String, Map<String, Variant<?>>> connection);

    public static class StateChanged extends DBusSignal {

        private final UInt32 state;

        public StateChanged(String path, UInt32 state) throws DBusException {
            super(path, state);
            this.state = state;
        }

        public UInt32 getState() {
            return state;
        }

    }

    public static class SecretsRequired extends DBusSignal {

        private final String message;
        private final List<String> secrets;

        public SecretsRequired(String path, String message, List<String> secrets) throws DBusException {
            super(path, message, secrets);
            this.message = message;
            this.secrets = secrets;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getSecrets() {
            return secrets;
        }

    }

    public static class Config extends DBusSignal {

        private final Map<String, Variant<?>> config;

        public Config(String path, Map<String, Variant<?>> config) throws DBusException {
            super(path, config);
            this.config = config;
        }

        public Map<String, Variant<?>> getConfig() {
            return config;
        }

    }

    public static class Ip4Config extends DBusSignal {

        private final Map<String, Variant<?>> ip4config;

        public Ip4Config(String path, Map<String, Variant<?>> ip4config) throws DBusException {
            super(path, ip4config);
            this.ip4config = ip4config;
        }

        public Map<String, Variant<?>> getIp4config() {
            return ip4config;
        }

    }

    public static class Ip6Config extends DBusSignal {

        private final Map<String, Variant<?>> ip6config;

        public Ip6Config(String path, Map<String, Variant<?>> ip6config) throws DBusException {
            super(path, ip6config);
            this.ip6config = ip6config;
        }

        public Map<String, Variant<?>> getIp6config() {
            return ip6config;
        }

    }

    public static class LoginBanner extends DBusSignal {

        private final String banner;

        public LoginBanner(String path, String banner) throws DBusException {
            super(path, banner);
            this.banner = banner;
        }

        public String getBanner() {
            return banner;
        }

    }

    public static class Failure extends DBusSignal {

        private final UInt32 reason;

        public Failure(String path, UInt32 reason) throws DBusException {
            super(path, reason);
            this.reason = reason;
        }

        public UInt32 getReason() {
            return reason;
        }

    }

}
