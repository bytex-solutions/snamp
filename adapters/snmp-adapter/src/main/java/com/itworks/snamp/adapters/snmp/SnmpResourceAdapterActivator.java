package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.osgi.service.jndi.JNDIContextManager;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceAdapterActivator extends ResourceAdapterActivator<SnmpResourceAdapter> {
    private static final class SnmpAdapterConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<SnmpAdapterConfigurationDescriptor> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         * @throws Exception An exception occurred during provider instantiation.
         */
        @Override
        public SnmpAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new SnmpAdapterConfigurationDescriptor();
        }
    }

    private static final class SnmpAdapterFactory implements ResourceAdapterFactory<SnmpResourceAdapter>{

        @Override
        public SnmpResourceAdapter createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) throws Exception {
            final JNDIContextManager contextManager = getDependency(RequiredServiceAccessor.class, JNDIContextManager.class, dependencies);
            return new SnmpResourceAdapter(adapterInstance, contextManager);
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    @SpecialUse
    public SnmpResourceAdapterActivator() {
        super(SnmpResourceAdapter.NAME,
                new SnmpAdapterFactory(),
                new RequiredService<?>[]{ new SimpleDependency<>(JNDIContextManager.class), SnmpAdapterLimitations.licenseReader },
                new SupportAdapterServiceManager<?, ?>[]{
                        new LicensingDescriptionServiceManager<>(SnmpAdapterLimitations.class, SnmpAdapterLimitations.fallbackFactory),
                        new SnmpAdapterConfigurationEntityDescriptionManager()
                });
    }
}
