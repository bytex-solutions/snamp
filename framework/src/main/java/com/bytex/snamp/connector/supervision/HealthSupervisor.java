package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.moa.DataAnalyzer;

import java.util.Set;

/**
 * Represents health check service used to supervise groups of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthSupervisor extends FrameworkService, DataAnalyzer {
    /**
     * Gets immutable set of groups configured for health check.
     * @return Immutable set of groups configured for health check.
     * @see ManagedResourceInfo#getGroupName()
     */
    Set<String> getWatchingGroups();

    /**
     * Gets health status of the specified group.
     * @param groupName Group of managed resources.
     * @return Health status of the group; or {@literal null}, if group is not configured for watching.
     */
    GroupStatus getHealthStatus(final String groupName);

    /**
     * Adds listener of health status.
     * @param listener Listener of health status to add.
     */
    void addHealthStatusEventListener(final GroupStatusEventListener listener);

    /**
     * Removes listener of health status.
     * @param listener Listener of health status to remove.
     */
    void removeHealthStatusEventListener(final GroupStatusEventListener listener);
}
