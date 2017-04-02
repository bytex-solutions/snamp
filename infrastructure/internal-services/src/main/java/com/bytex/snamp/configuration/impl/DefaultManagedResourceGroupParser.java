package com.bytex.snamp.configuration.impl;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DefaultManagedResourceGroupParser extends SerializableConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.resourceGroups";

    DefaultManagedResourceGroupParser() {
        super(SerializableAgentConfiguration::getResourceGroups, PID, SerializableManagedResourceGroupConfiguration.class);
    }
}
