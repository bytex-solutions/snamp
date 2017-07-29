package com.bytex.snamp.supervision.health.triggers;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

/**
 * Represents Groovy-based trigger.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class GroovyTrigger extends Scriptlet implements HealthStatusTrigger {
    private final ThreadLocal<ResourceGroupHealthStatus> previousStatus = new ThreadLocal<>();
    private final ThreadLocal<ResourceGroupHealthStatus> newStatus = new ThreadLocal<>();

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ResourceGroupHealthStatus getPreviousStatus(){
        return previousStatus.get();
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ResourceGroupHealthStatus getNewStatus(){
        return newStatus.get();
    }

    @Override
    public final void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus) {
        this.previousStatus.set(previousStatus);
        this.newStatus.set(newStatus);
        try {
            run();
        } finally {
            this.previousStatus.remove();
            this.newStatus.remove();
        }
    }
}
