package com.bytex.snamp.adapters.http;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 1.2
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

        @SuppressWarnings("unchecked")
        @Override
        public HttpAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) throws Exception {
            return new HttpAdapter(adapterInstance, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
        }
    }

    @SpecialUse
    public HttpAdapterActivator() {
        super(new RestAdapterFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportAdapterServiceManager<?, ?>[]{
                        new RestAdapterConfigurationManager()
                });
    }
}
