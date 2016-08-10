package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

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
                              final RequiredService<?>... dependencies){
        return new GroovyGateway(instanceName);
    }
}
