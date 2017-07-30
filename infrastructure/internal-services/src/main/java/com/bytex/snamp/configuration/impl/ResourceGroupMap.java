package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import javax.annotation.Nonnull;

/**
 * Represents list of managed resource groups.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class ResourceGroupMap extends SerializableEntityMap<SerializableManagedResourceGroupConfiguration> {
    private static final long serialVersionUID = -3661389588364513413L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ResourceGroupMap() {
    }

    @Override
    @Nonnull
    protected SerializableManagedResourceGroupConfiguration createValue() {
        final SerializableManagedResourceGroupConfiguration result = new SerializableManagedResourceGroupConfiguration();
        result.markAsModified();
        return result;
    }
}
