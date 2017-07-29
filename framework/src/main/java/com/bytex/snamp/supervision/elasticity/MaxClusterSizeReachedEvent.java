package com.bytex.snamp.supervision.elasticity;

import javax.annotation.Nonnull;

/**
 * Indicates that maximum cluster size is reached and number of resources in the cluster cannot be increased.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class MaxClusterSizeReachedEvent extends ScalingEvent {
    private static final long serialVersionUID = -4813059495619927911L;

    protected MaxClusterSizeReachedEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }
}
