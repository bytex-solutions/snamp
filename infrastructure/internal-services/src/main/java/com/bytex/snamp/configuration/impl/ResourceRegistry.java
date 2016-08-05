package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Serializable collection of managed resources.
 */
final class ResourceRegistry extends ConfigurationEntityRegistry<SerializableManagedResourceConfiguration> {
    private static final long serialVersionUID = 8031527910928209252L;

    @SpecialUse
    public ResourceRegistry() {
    }

    @Override
    protected SerializableManagedResourceConfiguration createEntity() {
        return new SerializableManagedResourceConfiguration();
    }
}
