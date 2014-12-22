package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;

import java.util.Collection;
import java.util.Map;

import static com.itworks.snamp.adapters.rest.RestAdapterConfigurationDescriptor.*;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RestAdapterActivator extends AbstractResourceAdapterActivator<RestAdapter> {

    private static final class RestAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<RestAdapterConfigurationDescriptor> {

        @Override
        protected RestAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new RestAdapterConfigurationDescriptor();
        }
    }

    public RestAdapterActivator(){
        super(RestAdapter.NAME,
                new RestAdapterConfigurationManager(),
                new LicensingDescriptionServiceManager<>(RestAdapterLimitations.class, RestAdapterLimitations.fallbackFactory));
    }

    /**
     * Initializes a new instance of the resource adapter.
     *
     * @param adapterInstanceName The name of the adapter instance.
     * @param parameters   A collection of initialization parameters.
     * @param dependencies A collection of dependencies used by adapter.
     * @return A new instance of the adapter.
     */
    @Override
    protected RestAdapter createAdapter(final String adapterInstanceName,
                                        final Map<String, String> parameters,
                                        final RequiredService<?>... dependencies) {
        RestAdapterLimitations.current().verifyServiceVersion(RestAdapter.class);
        final String port = parameters.containsKey(PORT_PARAM_NAME) ?
                parameters.get(PORT_PARAM_NAME) : Integer.toString(DEFAULT_PORT);
        final String host = parameters.containsKey(HOST_PARAM_NAME) ?
                parameters.get(HOST_PARAM_NAME) :
                DEFAULT_HOST;
        final String socketTimeout = parameters.containsKey(WEB_SOCKET_TIMEOUT_PARAM_NAME) ?
                parameters.get(WEB_SOCKET_TIMEOUT_PARAM_NAME) :
                Integer.toString(DEFAULT_TIMEOUT);
        return new RestAdapter(
                adapterInstanceName,
                Integer.valueOf(port),
                host,
                parameters.get(LOGIN_MODULE_NAME),
                parameters.get(DATE_FORMAT_PARAM_NAME),
                Integer.valueOf(socketTimeout),
                new JettyThreadPoolConfig(parameters, adapterInstanceName));
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
