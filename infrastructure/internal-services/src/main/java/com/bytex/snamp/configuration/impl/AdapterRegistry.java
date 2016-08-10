package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents collection of resource gateway.
 */
final class AdapterRegistry extends ConfigurationEntityRegistry<SerializableGatewayConfiguration> {
    private static final long serialVersionUID = 8142154170844526063L;

    @SpecialUse
    public AdapterRegistry() {
    }

    @Override
    protected SerializableGatewayConfiguration createEntity() {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        result.markAsModified();
        return result;
    }
}
