package com.bytex.snamp.gateway.nrdp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.GatewayActivator;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NRDPGatewayActivator extends GatewayActivator<NRDPGateway> {

    @SpecialUse
    public NRDPGatewayActivator(){
        super(NRDPGatewayActivator::newGateway, configurationDescriptor(NRDPGatewayConfigurationDescriptor::getInstance));
    }

    private static NRDPGateway newGateway(final String instanceName,
                                          final DependencyManager dependencies){
        return new NRDPGateway(instanceName);
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(ClusterMember.class));
    }
}
