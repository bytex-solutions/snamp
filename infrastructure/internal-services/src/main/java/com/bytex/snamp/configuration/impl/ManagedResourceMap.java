package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Serializable collection of managed resources.
 */
final class ManagedResourceMap extends SerializableEntityMap<SerializableManagedResourceConfiguration> {
    private static final long serialVersionUID = 8031527910928209252L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ManagedResourceMap() {
    }

    @Override
    protected SerializableManagedResourceConfiguration createValue() {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.markAsModified();
        return result;
    }
}
