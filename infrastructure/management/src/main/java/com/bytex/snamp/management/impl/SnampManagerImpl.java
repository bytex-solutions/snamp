package com.bytex.snamp.management.impl;

import com.bytex.snamp.management.AbstractSnampManager;

import java.util.logging.Logger;

/**
 * The type Snamp manager impl.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnampManagerImpl extends AbstractSnampManager {
    private static final class ResourceConnectorDescriptorImpl extends ResourceConnectorDescriptor{
        private static final long serialVersionUID = -9051897273537657012L;

        /**
         * Instantiates a new Resource connector descriptor impl.
         *
         * @param connectorName the connector name
         */
        protected ResourceConnectorDescriptorImpl(final String connectorName) {
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
        public ResourceAdapterDescriptorImpl(final String systemName) {
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
        return MonitoringUtils.getLogger();
    }
}
