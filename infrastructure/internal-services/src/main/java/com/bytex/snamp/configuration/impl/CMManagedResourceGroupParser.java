package com.bytex.snamp.configuration.impl;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMManagedResourceGroupParser extends SerializableConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.connector.groups";

    CMManagedResourceGroupParser() {
        super(PID, SerializableManagedResourceGroupConfiguration.class);
    }
}
