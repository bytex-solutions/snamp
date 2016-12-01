package com.bytex.snamp.management;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.logging.Logger;

/**
 * Represents management operations that can be applied to SNAMP-related software components.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnampManagerImpl extends AbstractSnampManager {
    private static final String LOGGER_NAME = "com.bytex.snamp.management";

    private static final class ResourceConnectorDescriptorImpl extends ResourceConnectorDescriptor{
        private static final long serialVersionUID = -9051897273537657012L;

        /**
         * Instantiates a new Resource connector descriptor impl.
         *
         * @param connectorName the connector name
         */
        private ResourceConnectorDescriptorImpl(final String connectorName) {
            super(connectorName);
        }
    }

    private final static class GatewayDescriptorImpl extends GatewayDescriptor {
        private static final long serialVersionUID = 6911837979438477985L;

        /**
         * Instantiates a new gateway descriptor impl.
         *
         * @param systemName the system name
         */
        private GatewayDescriptorImpl(final String systemName) {
            super(systemName);
        }
    }

    /**
     * Creates a new instance of the connector descriptor.
     *
     * @param systemName The name of the connector.
     * @return A new instance of the connector descriptor.
     */
    @Override
    protected ResourceConnectorDescriptorImpl createResourceConnectorDescriptor(final String systemName) {
        return new ResourceConnectorDescriptorImpl(systemName);
    }

    /**
     * Creates a new instance of the gateway descriptor.
     *
     * @param gatewayType Type of gateway.
     * @return A new instance of the gateway descriptor.
     */
    @Override
    protected GatewayDescriptorImpl createGatewayDescriptor(final String gatewayType) {
        return new GatewayDescriptorImpl(gatewayType);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }

    public static void restart(final BundleContext context) throws BundleException {
        //first, stop all gateway
        GatewayActivator.disableGateways(context);
        //second, stop all connector
        ManagedResourceActivator.disableConnectors(context);
        //third, start all connector
        ManagedResourceActivator.enableConnectors(context);
        //fourth, start all gateway
        GatewayActivator.enableGateways(context);
    }
}