package com.bytex.snamp.connector.health;

import java.io.Serializable;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum HealthCheckStatus implements Serializable {
    ONLINE,
    OFFLINE,
    UNKNOWN
}
