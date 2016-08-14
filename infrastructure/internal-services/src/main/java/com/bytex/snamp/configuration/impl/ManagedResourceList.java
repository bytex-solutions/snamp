package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Serializable collection of managed resources.
 */
final class ManagedResourceList extends ConfigurationEntityList<SerializableManagedResourceConfiguration> {
    private static final long serialVersionUID = 8031527910928209252L;

    @SpecialUse
    public ManagedResourceList() {
    }

    @Override
    protected SerializableManagedResourceConfiguration createEntity() {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.markAsModified();
        return result;
    }
}
