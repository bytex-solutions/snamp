package com.bytex.snamp.supervision.elasticity;

import javax.annotation.Nonnull;

/**
 * Indicates that cluster is scaled up.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class ScaleOutEvent extends ScalingEvent {
    private static final long serialVersionUID = 3233989177718986981L;

    protected ScaleOutEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }
}
