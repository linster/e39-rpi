package org.freedesktop.networkmanager;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class GetAppliedConnectionTuple<A, B> extends Tuple {
    @Position(0)
    private A connection;
    @Position(1)
    private B versionId;

    public GetAppliedConnectionTuple(A connection, B versionId) {
        this.connection = connection;
        this.versionId = versionId;
    }

    public A getConnection() {
        return connection;
    }

    public void setConnection(A connection) {
        this.connection = connection;
    }

    public B getVersionId() {
        return versionId;
    }

    public void setVersionId(B versionId) {
        this.versionId = versionId;
    }

}
