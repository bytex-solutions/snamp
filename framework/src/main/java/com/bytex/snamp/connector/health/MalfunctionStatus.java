package com.bytex.snamp.connector.health;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Indicates some malfunction.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class MalfunctionStatus extends HealthStatus {
    private static final long serialVersionUID = 1718771285813234068L;
    private final Map<String, Object> data;

    MalfunctionStatus(final int severity) {
        super(severity);
        data = new HashMap<>();
    }

    /**
     * Gets additional data associated with this status.
     *
     * @return Map with additional data associated with this instance.
     */
    public final Map<String, Object> getData() {
        return firstNonNull(data, ImmutableMap.of());
    }
}
