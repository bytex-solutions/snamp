package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.core.AbstractBundleActivator;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.GatewayActivator;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class InfluxGatewayActivator extends GatewayActivator<InfluxGateway> {

    public InfluxGatewayActivator() {
        super(InfluxGatewayActivator::newGateway, configurationDescriptor(InfluxGatewayConfigurationDescriptionProvider::getInstance));
    }

    private static InfluxGateway newGateway(final String gatewayInstance,
                                      final DependencyManager dependencies) throws Exception{
        return new InfluxGateway(gatewayInstance);
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(ClusterMember.class));
    }
}
