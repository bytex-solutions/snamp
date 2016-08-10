package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SysLogGatewayActivator extends GatewayActivator<SysLogGateway> {
    private static final class SysLogAdapterFactory implements ResourceAdapterFactory<SysLogGateway>{

        @Override
        public SysLogGateway createAdapter(final String adapterInstance,
                                           final RequiredService<?>... dependencies) {
            return new SysLogGateway(adapterInstance);
        }
    }

    private static final class SysLogConfigurationProvider extends ConfigurationEntityDescriptionManager<SysLogConfigurationDescriptor>{
        @Override
        protected SysLogConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new SysLogConfigurationDescriptor();
        }
    }

    @SpecialUse
    public SysLogGatewayActivator(){
        super(new SysLogAdapterFactory(), new SysLogConfigurationProvider());
    }
}
