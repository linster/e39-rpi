package org.freedesktop;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class AddAndActivateConnectionTuple<A, B> extends Tuple {
    @Position(0)
    private A path;
    @Position(1)
    private B activeConnection;

    public AddAndActivateConnectionTuple(A path, B activeConnection) {
        this.path = path;
        this.activeConnection = activeConnection;
    }

    public void setPath(A arg) {
        path = arg;
    }

    public A getPath() {
        return path;
    }

    public void setActiveConnection(B arg) {
        activeConnection = arg;
    }

    public B getActiveConnection() {
        return activeConnection;
    }

}
