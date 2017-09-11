package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.log.OSGiLogFactory;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class SnmpGatewayActivator extends GatewayActivator<SnmpGateway> {
    static {
        OSGiLogFactory.setup();
    }

    /**
     * Initializes a new instance of the gateway lifetime manager.
     */
    @SpecialUse(SpecialUse.Case.OSGi)
    public SnmpGatewayActivator() {
        super(SnmpGatewayActivator::newGateway,
                configurationDescriptor(SnmpGatewayDescriptionProvider::getInstance),
                requiredBy(SnmpGateway.class).require(JNDIContextManager.class, ThreadPoolRepository.class));
    }

    private static SnmpGateway newGateway(final String instanceName, final DependencyManager dependencies) {
        final JNDIContextManager contextManager = dependencies.getService(JNDIContextManager.class);
        return new SnmpGateway(instanceName, contextManager);
    }
}
