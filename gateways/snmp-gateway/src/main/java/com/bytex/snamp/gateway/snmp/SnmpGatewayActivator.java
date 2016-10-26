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

    /**
     * Initializes a new instance of the gateway lifetime manager.
     */
    @SpecialUse
    public SnmpGatewayActivator() {
        super(SnmpGatewayActivator::newGateway,
                new RequiredService<?>[]{ new SimpleDependency<>(JNDIContextManager.class), new SimpleDependency<>(ThreadPoolRepository.class) },
                new SupportGatewayServiceManager<?, ?>[]{
                        configurationDescriptor(SnmpGatewayDescriptionProvider::getInstance)
                });
    }

    private static SnmpGateway newGateway(final String instanceName, final RequiredService<?>... dependencies) {
        @SuppressWarnings("unchecked")
        final JNDIContextManager contextManager = getDependency(RequiredServiceAccessor.class, JNDIContextManager.class, dependencies);
        return new SnmpGateway(instanceName, contextManager);
    }
}
