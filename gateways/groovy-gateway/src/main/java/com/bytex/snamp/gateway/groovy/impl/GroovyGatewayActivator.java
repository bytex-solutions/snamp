package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class GroovyGatewayActivator extends GatewayActivator<GroovyGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public GroovyGatewayActivator(){
        super(GroovyGatewayActivator::newGateway, configurationDescriptor(GroovyGatewayConfigurationProvider::new));
    }

    private static GroovyGateway newGateway(final String instanceName,
                              final DependencyManager dependencies){
        return new GroovyGateway(instanceName);
    }
}
