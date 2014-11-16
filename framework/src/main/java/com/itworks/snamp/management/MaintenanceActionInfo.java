package com.itworks.snamp.management;

import com.itworks.snamp.Descriptive;

/**
 * Represents helper interface that can be implemented by enum
 * that describes maintenance actions.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MaintenanceActionInfo extends Descriptive {
    /**
     * Gets system name of this action,
     * @return The system name of this action.
     */
    public String getName();
}
