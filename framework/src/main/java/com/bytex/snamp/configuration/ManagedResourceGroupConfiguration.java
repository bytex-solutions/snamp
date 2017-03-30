package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

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

    /**
     * Instantiates a new configuration of the managed resource based on the group configuration; or merge
     * existing resource configuration with group configuration.
     * @param resourceName Name of the managed resource.
     * @param resources Repository of managed resources.
     * @return Modified managed resource.
     */
    default ManagedResourceConfiguration instantiateOrMerge(final String resourceName, final EntityMap<? extends ManagedResourceConfiguration> resources) {
        final ManagedResourceConfiguration resource;
        merge(resource = resources.getOrAdd(resourceName));
        return resource;
    }

    void merge(final ManagedResourceConfiguration resourceConfig);
}