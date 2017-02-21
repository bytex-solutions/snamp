package com.bytex.snamp.health;

import java.io.Serializable;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum HealthStatus implements Serializable {
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

    public final HealthStatus max(final HealthStatus other){
        return compareTo(other) >= 0 ? this : other;
    }

    public final HealthStatus min(final HealthStatus other){
        return compareTo(other) <= 0 ? this : other;
    }
}
