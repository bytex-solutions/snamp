package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * Represents activator of Nagios adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public final class NSCAGatewayActivator extends GatewayActivator<NSCAGateway> {
    private static final class NSCAResourceAdapterFactory implements ResourceAdapterFactory<NSCAGateway>{


        @Override
        public NSCAGateway createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) {
            return new NSCAGateway(adapterInstance);
        }
    }

    private static final class NSCAConfigurationProvider extends ConfigurationEntityDescriptionManager<NSCAGatewayConfigurationDescriptor>{

        @Override
        protected NSCAGatewayConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return NSCAGatewayConfigurationDescriptor.getInstance();
        }
    }

    @SpecialUse
    public NSCAGatewayActivator(){
        super(new NSCAResourceAdapterFactory(),  new NSCAConfigurationProvider());
    }
}
