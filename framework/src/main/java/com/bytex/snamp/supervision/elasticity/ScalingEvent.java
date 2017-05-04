package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.supervision.SupervisionEvent;

import javax.annotation.Nonnull;

/**
 * Informs about scaling operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScalingEvent extends SupervisionEvent {
    private static final long serialVersionUID = -3505563874608430847L;

    protected ScalingEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }
}
