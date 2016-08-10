package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyGatewayActivator extends GatewayActivator<GroovyGateway> {
    private static final class GroovyResourceAdapterFactory implements ResourceAdapterFactory<GroovyGateway>{

        @Override
        public GroovyGateway createAdapter(final String adapterInstance,
                                           final RequiredService<?>... dependencies) {
            return new GroovyGateway(adapterInstance);
        }
    }

    private static final class GroovyResourceAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<GroovyGatewayConfigurationProvider>{

        @Override
        protected GroovyGatewayConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new GroovyGatewayConfigurationProvider();
        }
    }

    @SpecialUse
    public GroovyGatewayActivator(){
        super(new GroovyResourceAdapterFactory(), new GroovyResourceAdapterConfigurationManager());
    }
}
