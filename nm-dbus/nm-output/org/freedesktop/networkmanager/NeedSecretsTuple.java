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

    public A getUsername() {
        return username;
    }

    public void setUsername(A username) {
        this.username = username;
    }

    public B getPassword() {
        return password;
    }

    public void setPassword(B password) {
        this.password = password;
    }

}
