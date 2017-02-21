package com.bytex.snamp.supervision;

import com.bytex.snamp.connector.health.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface GroupStatusDetails extends HealthStatusDetails {
    /**
     * Gets the most problematic resource.
     * @return The most problematic resource. Can be empty if status is {@link HealthStatus#OK}.
     */
    String getResourceName();
}
