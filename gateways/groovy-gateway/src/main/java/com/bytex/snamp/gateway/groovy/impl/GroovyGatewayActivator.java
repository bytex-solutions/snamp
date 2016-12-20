package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractBundleActivator;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.GatewayActivator;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyGatewayActivator extends GatewayActivator<GroovyGateway> {

    @SpecialUse
    public GroovyGatewayActivator(){
        super(GroovyGatewayActivator::newGateway, configurationDescriptor(GroovyGatewayConfigurationProvider::new));
    }

    private static GroovyGateway newGateway(final String instanceName,
                              final DependencyManager dependencies){
        return new GroovyGateway(instanceName);
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(ClusterMember.class));
    }
}
