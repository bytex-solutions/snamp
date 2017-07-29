package com.bytex.snamp.configuration;

/**
 * Represents group of managed resources.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface ManagedResourceGroupConfiguration extends ManagedResourceTemplate {
    /**
     * Merges configuration of the group into managed resource.
     * @param resource Resource to be modified.
     */
    void fillResourceConfig(final ManagedResourceConfiguration resource);
}