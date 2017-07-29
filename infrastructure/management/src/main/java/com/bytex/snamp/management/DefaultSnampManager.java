package com.bytex.snamp.management;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.supervision.SupervisorActivator;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.annotation.Nonnull;

/**
 * Represents management operations that can be applied to SNAMP-related software components.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class DefaultSnampManager extends AbstractSnampManager {

    private static final class DefaultResourceConnectorDescriptor extends ResourceConnectorDescriptor{
        private static final long serialVersionUID = -9051897273537657012L;

        /**
         * Instantiates a new Resource connector descriptor impl.
         *
         * @param connectorName the connector name
         */
        private DefaultResourceConnectorDescriptor(final String connectorName) {
            super(connectorName);
        }
    }

    private final static class DefaultGatewayDescriptor extends GatewayDescriptor {
        private static final long serialVersionUID = 6911837979438477985L;

        /**
         * Instantiates a new gateway descriptor impl.
         *
         * @param systemName the system name
         */
        private DefaultGatewayDescriptor(final String systemName) {
            super(systemName);
        }
    }

    private final static class DefaultSupervisorDescriptor extends SupervisorDescriptor{
        private static final long serialVersionUID = 4068696282423381190L;

        private DefaultSupervisorDescriptor(final String systemName) {
            super(systemName);
        }
    }

    /**
     * Gets runtime configuration of this service.
     *
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Nonnull
    @Override
    public ImmutableMap<String, String> getConfiguration() {
        return ImmutableMap.of();
    }

    /**
     * Creates a new instance of the supervisor descriptor.
     *
     * @param supervisorType Type of supervisor.
     * @return A new instance of the supervisor descriptor.
     */
    @Override
    @Nonnull
    protected DefaultSupervisorDescriptor createSupervisorDescriptor(final String supervisorType) {
        return new DefaultSupervisorDescriptor(supervisorType);
    }

    /**
     * Creates a new instance of the connector descriptor.
     *
     * @param systemName The name of the connector.
     * @return A new instance of the connector descriptor.
     */
    @Override
    @Nonnull
    protected DefaultResourceConnectorDescriptor createResourceConnectorDescriptor(final String systemName) {
        return new DefaultResourceConnectorDescriptor(systemName);
    }

    /**
     * Creates a new instance of the gateway descriptor.
     *
     * @param gatewayType Type of gateway.
     * @return A new instance of the gateway descriptor.
     */
    @Override
    @Nonnull
    protected DefaultGatewayDescriptor createGatewayDescriptor(final String gatewayType) {
        return new DefaultGatewayDescriptor(gatewayType);
    }

    public static void restart(final BundleContext context) throws BundleException {
        //the first, stop all gateway
        GatewayActivator.disableGateways(context);
        //the second, stop all supervisors
        SupervisorActivator.disableSupervisors(context);
        //the third, stop all connector
        ManagedResourceActivator.disableConnectors(context);
        //the fourth, start all supervisors
        SupervisorActivator.enableSupervisors(context);
        //the fifth, start all connector
        ManagedResourceActivator.enableConnectors(context);
        //the sixth, start all gateway
        GatewayActivator.enableGateways(context);
    }
}
