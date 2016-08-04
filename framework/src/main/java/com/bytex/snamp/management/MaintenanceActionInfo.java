package com.bytex.snamp.management;

import com.bytex.snamp.Localizable;

/**
 * Represents helper interface that can be implemented by enum
 * that describes maintenance actions.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface MaintenanceActionInfo extends Localizable {
    /**
     * Gets system name of this action,
     * @return The system name of this action.
     */
    String getName();
}
