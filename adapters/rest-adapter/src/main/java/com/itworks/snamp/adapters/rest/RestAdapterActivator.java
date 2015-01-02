package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.adapters.ResourceAdapterActivator;

import java.util.Collection;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RestAdapterActivator extends ResourceAdapterActivator<RestAdapter> {

    private static final class RestAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<RestAdapterConfigurationDescriptor> {

        @Override
        protected RestAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new RestAdapterConfigurationDescriptor();
        }
    }

    private static final class RestAdapterFactory implements ResourceAdapterFactory<RestAdapter>{

        @Override
        public RestAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) throws Exception {
            return new RestAdapter(adapterInstance);
        }
    }

    public RestAdapterActivator(){
        super(RestAdapter.NAME,
                new RestAdapterFactory(),
                new RestAdapterConfigurationManager(),
                new LicensingDescriptionServiceManager<>(RestAdapterLimitations.class, RestAdapterLimitations.fallbackFactory));
    }

    /**
     * Exposes additional adapter dependencies.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param dependencies A collection of dependencies to fill.
     */
    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(RestAdapterLimitations.licenseReader);
    }
}
