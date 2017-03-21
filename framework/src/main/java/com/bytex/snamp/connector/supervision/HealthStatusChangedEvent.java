package com.bytex.snamp.connector.supervision;

import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class HealthStatusChangedEvent extends EventObject {
    private static final long serialVersionUID = 2442700408424867171L;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    protected HealthStatusChangedEvent(final Object source) {
        super(source);
    }

    public abstract HealthStatus getNewStatus();

    public abstract HealthStatus getPreviousStatus();
}
