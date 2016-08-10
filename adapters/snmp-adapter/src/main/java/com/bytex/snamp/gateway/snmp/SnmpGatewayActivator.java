package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.log.OSGiLogFactory;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnmpGatewayActivator extends GatewayActivator<SnmpGateway> {
    static {
        OSGiLogFactory.setup();
    }

    private static final class SnmpAdapterConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<SnmpGatewayDescriptionProvider> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         * @throws Exception An exception occurred during provider instantiation.
         */
        @Override
        public SnmpGatewayDescriptionProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return SnmpGatewayDescriptionProvider.getInstance();
        }
    }

    private static final class SnmpAdapterFactory implements ResourceAdapterFactory<SnmpGateway>{

        @Override
        public SnmpGateway createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) throws Exception {
            @SuppressWarnings("unchecked")
            final JNDIContextManager contextManager = getDependency(RequiredServiceAccessor.class, JNDIContextManager.class, dependencies);
            return new SnmpGateway(adapterInstance, contextManager);
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    @SpecialUse
    public SnmpGatewayActivator() {
        super(new SnmpAdapterFactory(),
                new RequiredService<?>[]{ new SimpleDependency<>(JNDIContextManager.class), new SimpleDependency<>(ThreadPoolRepository.class) },
                new SupportAdapterServiceManager<?, ?>[]{
                        new SnmpAdapterConfigurationEntityDescriptionManager()
                });
    }
}
