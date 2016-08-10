package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class HttpGatewayActivator extends GatewayActivator<HttpGateway> {

    private static final class RestAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<HttpGatewayConfigurationDescriptor> {

        @Override
        protected HttpGatewayConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new HttpGatewayConfigurationDescriptor();
        }
    }

    private static final class RestAdapterFactory implements ResourceAdapterFactory<HttpGateway>{

        @SuppressWarnings("unchecked")
        @Override
        public HttpGateway createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) throws Exception {
            return new HttpGateway(adapterInstance, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
        }
    }

    @SpecialUse
    public HttpGatewayActivator() {
        super(new RestAdapterFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportAdapterServiceManager<?, ?>[]{
                        new RestAdapterConfigurationManager()
                });
    }
}
