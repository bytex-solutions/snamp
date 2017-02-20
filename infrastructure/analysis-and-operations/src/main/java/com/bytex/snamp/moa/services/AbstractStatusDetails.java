package com.bytex.snamp.moa.services;

import com.bytex.snamp.moa.watching.HealthCheckStatusDetails;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractStatusDetails implements HealthCheckStatusDetails {
    private final String resourceName;

    AbstractStatusDetails(final String resourceName){
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    @Override
    public final String getResourceName() {
        return resourceName;
    }

    final AbstractStatusDetails replaceWith(final AbstractStatusDetails other) {
        /*
            This status can be replaced with new status only when:
                1. When new status is greater than previous
                2. When the status is upgraded for the same resource
         */
        return other.getStatus().compareTo(getStatus()) >= 0 || other.getResourceName().equals(resourceName) ? other : this;
    }
}
