package com.bytex.snamp.gateway.nagios;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NagiosGatewayActivator extends GatewayActivator<NagiosGateway> {
    private static final class NagiosAdapterFactory implements ResourceAdapterFactory<NagiosGateway>{

        @SuppressWarnings("unchecked")
        @Override
        public NagiosGateway createAdapter(final String adapterInstance,
                                           final RequiredService<?>... dependencies) {
            return new NagiosGateway(adapterInstance, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
        }
    }

    private static final class NagiosConfigurationProvider extends ConfigurationEntityDescriptionManager<NagiosGatewayConfigurationDescriptor>{

        @Override
        protected NagiosGatewayConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new NagiosGatewayConfigurationDescriptor();
        }
    }

    @SpecialUse
    public NagiosGatewayActivator() {
        super(new NagiosAdapterFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportAdapterServiceManager<?, ?>[]{new NagiosConfigurationProvider()});
    }
}
