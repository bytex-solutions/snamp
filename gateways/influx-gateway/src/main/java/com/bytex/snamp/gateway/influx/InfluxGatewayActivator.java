package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class InfluxGatewayActivator extends GatewayActivator<InfluxGateway> {

    public InfluxGatewayActivator() {
        super(InfluxGatewayActivator::newGateway, configurationDescriptor(InfluxGatewayConfigurationDescriptionProvider::getInstance));
    }

    private static InfluxGateway newGateway(final String gatewayInstance,
                                      final DependencyManager dependencies) {
        return new InfluxGateway(gatewayInstance);
    }
}
