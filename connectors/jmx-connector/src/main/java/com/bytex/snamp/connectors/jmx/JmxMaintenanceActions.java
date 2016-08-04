package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.management.MaintenanceActionInfo;

import java.util.Locale;

enum JmxMaintenanceActions implements MaintenanceActionInfo {
    SIMULATE_CONNECTION_ABORT("simulateConnectionAbort");

    private final String name;

    JmxMaintenanceActions(final String name){
        this.name = name;
    }

    /**
     * Gets system name of this action,
     *
     * @return The system name of this action.
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * Gets description of this action.
     *
     * @param loc The locale of the description.
     * @return The description of this action.
     */
    @Override
    public final String toString(final Locale loc) {
        return "";
    }
}
