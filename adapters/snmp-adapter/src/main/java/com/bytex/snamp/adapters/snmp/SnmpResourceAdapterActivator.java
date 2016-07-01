package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.log.OSGiLogFactory;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SnmpResourceAdapterActivator extends ResourceAdapterActivator<SnmpResourceAdapter> {
    static {
        OSGiLogFactory.setup();
    }

    private static final class SnmpAdapterConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<SnmpAdapterDescriptionProvider> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         * @throws Exception An exception occurred during provider instantiation.
         */
        @Override
        public SnmpAdapterDescriptionProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return SnmpAdapterDescriptionProvider.getInstance();
        }
    }

    private static final class SnmpAdapterFactory implements ResourceAdapterFactory<SnmpResourceAdapter>{

        @Override
        public SnmpResourceAdapter createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) throws Exception {
            @SuppressWarnings("unchecked")
            final JNDIContextManager contextManager = getDependency(RequiredServiceAccessor.class, JNDIContextManager.class, dependencies);
            return new SnmpResourceAdapter(adapterInstance, contextManager);
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    @SpecialUse
    public SnmpResourceAdapterActivator() {
        super(new SnmpAdapterFactory(),
                new RequiredService<?>[]{ new SimpleDependency<>(JNDIContextManager.class), new SimpleDependency<>(ThreadPoolRepository.class) },
                new SupportAdapterServiceManager<?, ?>[]{
                        new SnmpAdapterConfigurationEntityDescriptionManager()
                });
    }
}
