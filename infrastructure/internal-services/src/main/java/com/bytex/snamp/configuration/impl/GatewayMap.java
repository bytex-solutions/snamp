package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents collection of gateways.
 */
final class GatewayMap extends SerializableEntityMap<SerializableGatewayConfiguration> {
    private static final long serialVersionUID = 8142154170844526063L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public GatewayMap() {
    }

    @Override
    protected SerializableGatewayConfiguration createValue() {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        result.markAsModified();
        return result;
    }
}
