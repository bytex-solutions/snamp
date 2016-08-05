package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Created by Роман on 04.08.2016.
 */
final class AdapterRegistry extends ConfigurationEntityRegistry<SerializableResourceAdapterConfiguration> {
    private static final long serialVersionUID = 8142154170844526063L;

    @SpecialUse
    public AdapterRegistry() {
    }

    @Override
    protected SerializableResourceAdapterConfiguration createEntity() {
        return new SerializableResourceAdapterConfiguration();
    }
}
