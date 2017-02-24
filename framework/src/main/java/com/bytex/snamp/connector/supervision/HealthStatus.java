package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.Localizable;

import java.io.Serializable;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatus extends Serializable, Comparable<HealthStatus>, Localizable {
    /**
     * Indicates that managed resource is in critical state (potentially unavailable).
     *
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    boolean isCritical();

    /**
     * Gets status code that uniquely identifies this type of status.
     *
     * @return Status code.
     */
    int getStatusCode();

    static <S extends HealthStatus> S max(final S left, final S right) {
        return left.compareTo(right) >= 0 ? left : right;
    }
}
