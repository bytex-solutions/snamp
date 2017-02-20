package com.bytex.snamp.moa.services;

import com.bytex.snamp.moa.watching.HealthCheckStatusDetails;
import com.bytex.snamp.moa.watching.RootCause;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractStatusDetails<R extends RootCause> implements HealthCheckStatusDetails {
    private final String resourceName;
    private final R rootCause;

    AbstractStatusDetails(final String resourceName,
                          final R rootCause){
        this.resourceName = Objects.requireNonNull(resourceName);
        this.rootCause = rootCause;
    }

    @Override
    public final String getResourceName() {
        return resourceName;
    }

    @Override
    public final R getRootCause() {
        return rootCause;
    }

    final AbstractStatusDetails<?> replaceWith(final AbstractStatusDetails<?> other){
        /*
            This status can be replaced with new status only when:
                1. When new status is greater than previous
                2. When the status is upgraded for the same resource
         */
        return other.getStatus().compareTo(getStatus()) >= 0 || other.getResourceName().equals(resourceName) ? other : this;
    }
}
