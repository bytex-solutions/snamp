package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SupervisorMap extends SerializableEntityMap<SerializableSupervisorConfiguration> {
    private static final long serialVersionUID = -1512394714178712018L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SupervisorMap() {
    }

    @Override
    @Nonnull
    SerializableSupervisorConfiguration createValue() {
        final SerializableSupervisorConfiguration result = new SerializableSupervisorConfiguration();
        result.markAsModified();
        return result;
    }
}
