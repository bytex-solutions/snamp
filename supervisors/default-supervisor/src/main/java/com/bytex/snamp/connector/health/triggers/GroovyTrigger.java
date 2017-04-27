package com.bytex.snamp.connector.health.triggers;

import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

import javax.annotation.Nonnull;

/**
 * Represents Groovy-based trigger.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyTrigger extends Scriptlet implements HealthStatusEventListener {
    @Override
    public final void statusChanged(@Nonnull final HealthStatusChangedEvent event, final Object handback) {
        statusChanged(event.getPreviousStatus(), event.getNewStatus());
    }

    protected abstract void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus);
}
