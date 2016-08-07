package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents collection of resource adapters.
 */
final class AdapterRegistry extends ConfigurationEntityRegistry<SerializableResourceAdapterConfiguration> {
    private static final long serialVersionUID = 8142154170844526063L;

    @SpecialUse
    public AdapterRegistry() {
    }

    @Override
    protected SerializableResourceAdapterConfiguration createEntity() {
        final SerializableResourceAdapterConfiguration result = new SerializableResourceAdapterConfiguration();
        result.markAsModified();
        return result;
    }
}
