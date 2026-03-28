package org.freedesktop;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class GetLoggingTuple<A, B> extends Tuple {
    @Position(0)
    private A level;
    @Position(1)
    private B domains;

    public GetLoggingTuple(A level, B domains) {
        this.level = level;
        this.domains = domains;
    }

    public void setLevel(A arg) {
        level = arg;
    }

    public A getLevel() {
        return level;
    }

    public void setDomains(B arg) {
        domains = arg;
    }

    public B getDomains() {
        return domains;
    }

}
