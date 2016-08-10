package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.management.AbstractSnampManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.logging.Logger;

/**
 * The type Snamp manager impl.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnampManagerImpl extends AbstractSnampManager {
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

    private final static class ResourceAdapterDescriptorImpl extends ResourceAdapterDescriptor{
        private static final long serialVersionUID = 6911837979438477985L;

        /**
         * Instantiates a new Resource adapter descriptor impl.
         *
         * @param systemName the system name
         */
        private ResourceAdapterDescriptorImpl(final String systemName) {
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
     * Creates a new instance of the resource adapter descriptor.
     *
     * @param systemName The system name of the adapter.
     * @return A new instance of the resource adapter descriptor.
     */
    @Override
    protected ResourceAdapterDescriptorImpl createResourceAdapterDescriptor(final String systemName) {
        return new ResourceAdapterDescriptorImpl(systemName);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return SnampCoreMBean.getLoggerImpl();
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
