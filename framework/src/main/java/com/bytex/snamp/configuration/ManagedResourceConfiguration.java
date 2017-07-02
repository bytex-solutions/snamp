package com.bytex.snamp.configuration;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents management target configuration (back-end management information providers).
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface ManagedResourceConfiguration extends ManagedResourceTemplate, ManagedResourceInfo {
    /**
     * Name of the configuration property that contains name of the group.
     */
    String GROUP_NAME_PROPERTY = "group";

    /**
     * Sets resource group for this resource.
     * @param value The name of the resource group. Cannot be {@literal null}.
     */
    default void setGroupName(final String value) {
        if (isNullOrEmpty(value))
            remove(GROUP_NAME_PROPERTY);
        else
            put(GROUP_NAME_PROPERTY, value);
    }

    /**
     * Gets name of resource group.
     * @return Name of resource group; or empty string, if group is not assigned.
     */
    @Override
    default String getGroupName(){
        return getOrDefault(GROUP_NAME_PROPERTY, "");
    }

    /**
     * Gets the management target connection string.
     * @return The connection string that is used to connect to the management server.
     */
    @Override
    String getConnectionString();

    /**
     * Sets the management target connection string.
     * @param connectionString The connection string that is used to connect to the management server.
     */
    void setConnectionString(final String connectionString);

    /**
     * Gets a set of overridden configuration properties in this resource.
     * @return A set of overridden configuration properties in this resource.
     */
    Set<String> getOverriddenProperties();

    default void overrideProperties(final Collection<String> properties){
        getOverriddenProperties().retainAll(properties);
        getOverriddenProperties().addAll(properties);
    }
}
