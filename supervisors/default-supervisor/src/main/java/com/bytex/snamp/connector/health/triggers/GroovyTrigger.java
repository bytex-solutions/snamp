package com.bytex.snamp.connector.health.triggers;

import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

/**
 * Represents Groovy-based trigger.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyTrigger extends Scriptlet implements HealthStatusTrigger {
    @Override
    public abstract void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus);
}
