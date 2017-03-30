package com.bytex.snamp.configuration;

/**
 * Represents group of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ManagedResourceGroupConfiguration extends ManagedResourceTemplate {
    /**
     * Gets supervisor name used for this group.
     *
     * @return Supervisor name.
     */
    SupervisorConfiguration getSupervisor();
}