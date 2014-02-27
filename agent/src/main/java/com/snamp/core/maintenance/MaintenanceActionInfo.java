package com.snamp.core.maintenance;

import java.util.Locale;

/**
 * Represents helper interface that can be implemented by enum
 * that describes maintenance actions.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MaintenanceActionInfo {
    /**
     * Gets system name of this action,
     * @return The system name of this action.
     */
    public String getName();

    /**
     * Gets description of this action.
     * @param loc The locale of the description.
     * @return The description of this action.
     */
    public String getDescription(final Locale loc);
}
