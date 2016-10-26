package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents collection of gateways.
 */
final class GatewayList extends ConfigurationEntityList<SerializableGatewayConfiguration> {
    private static final long serialVersionUID = 8142154170844526063L;

    @SpecialUse
    public GatewayList() {
    }

    @Override
    protected SerializableGatewayConfiguration createEntity() {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        result.markAsModified();
        return result;
    }
}
