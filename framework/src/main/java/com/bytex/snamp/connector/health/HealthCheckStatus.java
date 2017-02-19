package com.bytex.snamp.connector.health;

import java.io.Serializable;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum HealthCheckStatus implements Serializable {
    /**
     * Service is online
     */
    OK,

    /**
     * Something wrong with component.
     */
    SUSPICIOUS,

    /**
     * The component is offline or not working.
     */
    MALFUNCTION;

    public final HealthCheckStatus max(final HealthCheckStatus other){
        return compareTo(other) >= 0 ? this : other;
    }

    public final HealthCheckStatus min(final HealthCheckStatus other){
        return compareTo(other) <= 0 ? this : other;
    }
}
