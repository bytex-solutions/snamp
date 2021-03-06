package com.bytex.snamp.configuration;
import java.util.Collection;
import java.util.Set;

/**
 * Represents management target configuration (back-end management information providers).
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface ManagedResourceConfiguration extends ManagedResourceTemplate, ManagedResourceInfo {
    /**
     * Sets resource group for this resource.
     * @param value The name of the resource group. Cannot be {@literal null}.
     */
    void setGroupName(final String value);

    /**
     * Gets name of resource group.
     * @return Name of resource group; or empty string, if group is not assigned.
     */
    @Override
    String getGroupName();
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
