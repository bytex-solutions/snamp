package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents general information about managed resource.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ManagedResourceInfo extends Map<String, String> {
    /**
     * Gets connection string uniquely representing remote resource.
     * @return Connection string to the resource.
     */
    String getConnectionString();

    /**
     * Gets name of group of this managed resource.
     * @return Name of the group of this managed resource.
     */
    String getGroupName();
}