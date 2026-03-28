package org.freedesktop.networkmanager;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class AddConnection2Tuple<A, B> extends Tuple {
    @Position(0)
    private A path;
    @Position(1)
    private B result;

    public AddConnection2Tuple(A path, B result) {
        this.path = path;
        this.result = result;
    }

    public void setPath(A arg) {
        path = arg;
    }

    public A getPath() {
        return path;
    }

    public void setResult(B arg) {
        result = arg;
    }

    public B getResult() {
        return result;
    }

}
