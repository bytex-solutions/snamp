package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class InfluxGatewayActivator extends GatewayActivator<InfluxGateway> {
    public InfluxGatewayActivator() {
        super(InfluxGatewayActivator::newGateway);
    }

    private static InfluxGateway newGateway(final String gatewayInstance,
                                      final RequiredService<?>... dependencies) throws Exception{
        return new InfluxGateway(gatewayInstance);
    }
}
