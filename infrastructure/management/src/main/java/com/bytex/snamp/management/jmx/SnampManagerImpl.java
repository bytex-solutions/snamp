package com.bytex.snamp.management.jmx;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.security.LoginConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * The type Snamp manager impl.
 * @author Roman Sakno
 * @version 1.2
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
        return MonitoringUtils.getLogger();
    }

    public static void restart(final BundleContext context) throws BundleException {
        //first, stop all adapters
        ResourceAdapterActivator.stopResourceAdapters(context);
        //second, stop all connectors
        ManagedResourceActivator.stopResourceConnectors(context);
        //third, start all connectors
        ManagedResourceActivator.startResourceConnectors(context);
        //fourth, start all adapters
        ResourceAdapterActivator.startResourceAdapters(context);
    }

    public static void dumpJaasConfiguration(final BundleContext context, final Writer out) throws IOException {
        final ServiceHolder<LoginConfigurationManager> manager = ServiceHolder.tryCreate(context, LoginConfigurationManager.class);
        if (manager != null)
            try {
                manager.get().dumpConfiguration(out);
            } finally {
                manager.release(context);
            }
    }

    public static void saveJaasConfiguration(final BundleContext context, final Reader in) throws IOException {
        final ServiceHolder<LoginConfigurationManager> manager = ServiceHolder.tryCreate(context, LoginConfigurationManager.class);
        if (manager != null)
            try {
                if (in == null)
                    manager.get().resetConfiguration();
                else
                    manager.get().loadConfiguration(in);
            } catch (final IOException e) {
                throw e;
            } catch (final Exception e) {
                throw new IOException(e);
            } finally {
                manager.release(context);
            }
    }
}
