package org.freedesktop.networkmanager;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class NeedSecretsTuple<A, B> extends Tuple {
    @Position(0)
    private A username;
    @Position(1)
    private B password;

    public NeedSecretsTuple(A username, B password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(A arg) {
        username = arg;
    }

    public A getUsername() {
        return username;
    }

    public void setPassword(B arg) {
        password = arg;
    }

    public B getPassword() {
        return password;
    }

}
