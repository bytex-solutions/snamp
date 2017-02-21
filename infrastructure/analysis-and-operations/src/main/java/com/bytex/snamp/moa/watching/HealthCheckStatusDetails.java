package com.bytex.snamp.moa.watching;

import com.bytex.snamp.health.*;
import com.bytex.snamp.health.NoRootCause;
import com.bytex.snamp.health.ResourceUnavailable;
import com.bytex.snamp.health.RootCause;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckStatusDetails {
    /**
     * Gets health check status.
     * @return Health check status.
     */
    HealthStatus getStatus();

    /**
     * Gets the most problematic resource.
     * @return The most problematic resource. Can be empty if status is {@link HealthStatus#OK}.
     */
    String getResourceName();

    /**
     * Gets root cause of the current status.
     * @return Root cause of the current status; or {@link com.bytex.snamp.health.NoRootCause} if status is {@link HealthStatus#OK}.
     * @see com.bytex.snamp.health.CausedByAttribute
     * @see ResourceUnavailable
     * @see NoRootCause
     */
    RootCause getRootCause();
}
