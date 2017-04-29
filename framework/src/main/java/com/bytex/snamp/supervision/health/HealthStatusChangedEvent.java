package com.bytex.snamp.supervision.health;

import com.bytex.snamp.supervision.SupervisionEvent;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class HealthStatusChangedEvent extends SupervisionEvent {
    private static final long serialVersionUID = 2442700408424867171L;

    protected HealthStatusChangedEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }

    public abstract ResourceGroupHealthStatus getNewStatus();

    public abstract ResourceGroupHealthStatus getPreviousStatus();
}
