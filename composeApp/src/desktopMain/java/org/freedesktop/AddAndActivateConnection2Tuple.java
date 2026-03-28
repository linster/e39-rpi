package org.freedesktop;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class AddAndActivateConnection2Tuple<A, B, C> extends Tuple {
    @Position(0)
    private A path;
    @Position(1)
    private B activeConnection;
    @Position(2)
    private C result;

    public AddAndActivateConnection2Tuple(A path, B activeConnection, C result) {
        this.path = path;
        this.activeConnection = activeConnection;
        this.result = result;
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

    public void setResult(C arg) {
        result = arg;
    }

    public C getResult() {
        return result;
    }

}
