package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the gateway configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableGatewayConfiguration extends ImmutableTypedEntityConfiguration implements GatewayConfiguration {
    private static final long serialVersionUID = 3793264109153847204L;

    ImmutableGatewayConfiguration(final GatewayConfiguration configuration){
        super(configuration);
    }

    @Override
    public ImmutableGatewayConfiguration asReadOnly() {
        return this;
    }
}
