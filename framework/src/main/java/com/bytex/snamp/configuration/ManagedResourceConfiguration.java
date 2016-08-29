package com.bytex.snamp.configuration;

/**
 * Represents management target configuration (back-end management information providers).
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface ManagedResourceConfiguration extends ManagedResourceTemplate {
    String GROUP_NAME_PROPERTY = "group";

    /**
     * Sets resource group for this resource.
     * @param value The name of the resource group. Cannot be {@literal null}.
     */
    default void setGroupName(final String value){
        getParameters().put(GROUP_NAME_PROPERTY, value);
    }

    /**
     * Gets name of resource group.
     * @return Name of resource group; or empty string, if group is not assigned.
     */
    default String getGroupName(){
        return getParameters().get(GROUP_NAME_PROPERTY);
    }

    /**
     * Gets the management target connection string.
     * @return The connection string that is used to connect to the management server.
     */
    String getConnectionString();

    /**
     * Sets the management target connection string.
     * @param connectionString The connection string that is used to connect to the management server.
     */
    void setConnectionString(final String connectionString);
}
