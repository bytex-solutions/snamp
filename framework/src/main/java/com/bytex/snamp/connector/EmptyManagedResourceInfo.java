package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.internal.ImmutableEmptyMap;

/**
 * Represents empty and immutable configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class EmptyManagedResourceInfo extends ImmutableEmptyMap<String, String> implements ManagedResourceInfo {
    /**
     * Gets connection string uniquely representing remote resource.
     *
     * @return Connection string to the resource.
     */
    @Override
    public String getConnectionString() {
        return "";
    }

    /**
     * Gets name of group of this managed resource.
     *
     * @return Name of the group of this managed resource.
     */
    @Override
    public String getGroupName() {
        return "";
    }
}
