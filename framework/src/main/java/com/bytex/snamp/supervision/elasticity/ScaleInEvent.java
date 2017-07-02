package com.bytex.snamp.supervision.elasticity;

import javax.annotation.Nonnull;

/**
 * Indicates that cluster is scaled down.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ScaleInEvent extends ScalingEvent {
    private static final long serialVersionUID = -265208770427931388L;

    protected ScaleInEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }
}
