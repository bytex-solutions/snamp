package com.itworks.snamp.adapters.http;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HttpAdapterActivator extends ResourceAdapterActivator<HttpAdapter> {

    private static final class RestAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<HttpAdapterConfigurationDescriptor> {

        @Override
        protected HttpAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new HttpAdapterConfigurationDescriptor();
        }
    }

    private static final class RestAdapterFactory implements ResourceAdapterFactory<HttpAdapter>{

        @Override
        public HttpAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) throws Exception {
            return new HttpAdapter(adapterInstance, getDependency(RequiredServiceAccessor.class, HttpService.class));
        }
    }

    @SpecialUse
    public HttpAdapterActivator() {
        super(HttpAdapter.NAME,
                new RestAdapterFactory(),
                new RequiredService<?>[]{HttpAdapterLimitations.licenseReader, new SimpleDependency<>(HttpService.class)},
                new SupportAdapterServiceManager<?, ?>[]{
                        new RestAdapterConfigurationManager(),
                        new LicensingDescriptionServiceManager<>(HttpAdapterLimitations.class, HttpAdapterLimitations.fallbackFactory)
                });
    }
}
