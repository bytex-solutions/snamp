package com.bytex.snamp.connector.supervision;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface GroupStatus extends HealthStatus {
    /**
     * Gets the most problematic resource.
     * @return The most problematic resource. Can be empty if status is {@link OkStatus}.
     */
    String getResourceName();

    default GroupStatus combine(@Nonnull final GroupStatus newStatus) {
        return newStatus.compareTo(this) >= 0 || newStatus.getResourceName().equals(getResourceName()) ? newStatus : this;
    }
}
