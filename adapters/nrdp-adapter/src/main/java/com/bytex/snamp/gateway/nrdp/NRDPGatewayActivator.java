package com.bytex.snamp.gateway.nrdp;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NRDPGatewayActivator extends GatewayActivator<NRDPGateway> {
    private static final class NRDPResourceAdapterFactory implements ResourceAdapterFactory<NRDPGateway>{


        @Override
        public NRDPGateway createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) {
            return new NRDPGateway(adapterInstance);
        }
    }

    private static final class NRDPConfigurationProvider extends ConfigurationEntityDescriptionManager<NRDPGatewayConfigurationDescriptor>{

        @Override
        protected NRDPGatewayConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return NRDPGatewayConfigurationDescriptor.getInstance();
        }
    }

    @SpecialUse
    public NRDPGatewayActivator(){
        super(new NRDPResourceAdapterFactory(),  new NRDPConfigurationProvider());
    }

}
