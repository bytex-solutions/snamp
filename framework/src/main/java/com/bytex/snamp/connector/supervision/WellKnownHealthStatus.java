package com.bytex.snamp.connector.supervision;

import java.util.Locale;

/**
 * Represents well-known health check status that can be interpreted by SNAMP correctly.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see OkStatus
 * @see MalfunctionStatus
 */
public abstract class WellKnownHealthStatus implements HealthStatus {
    private static final long serialVersionUID = -8700097915541124870L;
    private final int code;

    WellKnownHealthStatus(final int statusCode){
        code = statusCode;
    }


    @Override
    public abstract boolean isCritical();


    @Override
    public final int getStatusCode(){
        return code;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
}
