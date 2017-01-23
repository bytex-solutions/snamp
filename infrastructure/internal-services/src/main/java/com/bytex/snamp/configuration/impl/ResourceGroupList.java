package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents list of managed resource groups.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ResourceGroupList extends ConfigurationEntityList<SerializableManagedResourceGroupConfiguration> {
    private static final long serialVersionUID = -3661389588364513413L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ResourceGroupList() {
    }

    @Override
    protected SerializableManagedResourceGroupConfiguration createEntity() {
        final SerializableManagedResourceGroupConfiguration result = new SerializableManagedResourceGroupConfiguration();
        result.markAsModified();
        return result;
    }
}
