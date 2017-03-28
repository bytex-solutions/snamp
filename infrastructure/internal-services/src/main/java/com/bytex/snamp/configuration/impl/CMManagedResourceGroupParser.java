package com.bytex.snamp.configuration.impl;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMManagedResourceGroupParser extends SerializableConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.resourceGroups";

    CMManagedResourceGroupParser() {
        super(PID, SerializableManagedResourceGroupConfiguration.class);
    }

    @Nonnull
    @Override
    public SerializableEntityMap<SerializableManagedResourceGroupConfiguration> apply(@Nonnull final SerializableAgentConfiguration owner) {
        return owner.getResourceGroups();
    }
}
