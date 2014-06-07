package com.itworks.snamp.monitoring.impl;

import com.itworks.snamp.management.AbstractSnampManager;
import org.osgi.framework.BundleContext;

import java.util.logging.Logger;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnampManagerImpl extends AbstractSnampManager {
    private static final class ResourceConnectorDescriptorImpl extends ResourceConnectorDescriptor{

        protected ResourceConnectorDescriptorImpl(final String connectorName) {
            super(connectorName);
        }

        /**
         * Represents the bundle context of the derived class.
         * <p>
         * Use {@link com.itworks.snamp.internal.Utils#getBundleContextByObject(Object)} with
         * {@literal this} parameter to implement this method.
         * </p>
         *
         * @return The bundle context of the derived class.
         */
        @Override
        protected BundleContext getItselfContext() {
            return getBundleContextByObject(this);
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
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return MonitoringUtils.getLogger();
    }
}
