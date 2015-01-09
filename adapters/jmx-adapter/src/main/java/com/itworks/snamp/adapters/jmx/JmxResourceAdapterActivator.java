package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.ResourceAdapterActivator;

/**
 * Represents JMX resource adapter activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxResourceAdapterActivator extends ResourceAdapterActivator<JmxResourceAdapter> {
    private static final class JmxConfigurationDescriptor extends ConfigurationEntityDescriptionManager<JmxAdapterConfigurationProvider> {

        @Override
        protected JmxAdapterConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new JmxAdapterConfigurationProvider();
        }
    }

    private static final class JmxAdapterFactory implements ResourceAdapterFactory<JmxResourceAdapter>{

        @Override
        public JmxResourceAdapter createAdapter(final String adapterInstance,
                                                final RequiredService<?>... dependencies) throws Exception {
            return new JmxResourceAdapter(adapterInstance);
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    public JmxResourceAdapterActivator() {
        super(JmxResourceAdapter.NAME,
                new JmxAdapterFactory(),
                new RequiredService<?>[]{JmxAdapterLicenseLimitations.licenseReader},
                new SupportAdapterServiceManager<?, ?>[]{
                        new JmxConfigurationDescriptor(),
                        new LicensingDescriptionServiceManager<>(JmxAdapterLicenseLimitations.class, JmxAdapterLicenseLimitations.fallbackFactory)
                });
    }
}