package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.concurrent.FutureThread;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.management.AbstractMaintainable;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.JMException;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.ManagedResourceConnectorClient.getConnectorType;
import static com.itworks.snamp.internal.Utils.getBundleContextByObject;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxConnectorBundleActivator extends ManagedResourceActivator<JmxConnector> {

    private static final class JmxMaintenanceService extends AbstractAggregator implements Maintainable{

        private JmxMaintenanceService(){
        }

        /**
         * Returns read-only map of maintenance actions.
         *
         * @return Read-only map of maintenance action,
         */
        @Override
        public Set<String> getActions() {
            return AbstractMaintainable.getMaintenanceActions(JmxMaintenanceActions.class);
        }

        /**
         * Returns human-readable description of the specified maintenance action that
         * includes description of the arguments string.
         *
         * @param actionName The name of the maintenance action.
         * @param loc        Target locale of the action description.
         * @return Localized description of the action.
         */
        @Override
        public String getActionDescription(final String actionName, final Locale loc) {
            return AbstractMaintainable.getActionDescription(JmxMaintenanceActions.class, actionName, loc);
        }

        /**
         * Invokes maintenance action.
         *
         * @param actionName The name of the action to invoke.
         * @param arguments  The action invocation command line. May be {@literal null} or empty for parameterless
         *                   action.
         * @param loc        Localization of the action arguments string and invocation result.
         * @return The localized result of the action invocation; or {@literal null}, if the specified
         * action doesn't exist.
         */
        @Override
        @SpecialUse
        public Future<String> doAction(final String actionName, final String arguments, final Locale loc) {
            if(Objects.equals(actionName, JmxMaintenanceActions.SIMULATE_CONNECTION_ABORT.getName())){
                final BundleContext context = getBundleContextByObject(this);
                final Map<String, ServiceReference<ManagedResourceConnector>> connectors = ManagedResourceConnectorClient.getConnectors(context);
                final FutureThread<String> result = new FutureThread<>(new Callable<String>() {
                    @Override
                    public final String call() throws IOException, InterruptedException{
                        for(final ServiceReference<ManagedResourceConnector> ref: connectors.values())
                            if(Objects.equals(getConnectorType(ref), JmxConnector.NAME))
                                try{
                                    final ManagedResourceConnector connector = context.getService(ref);
                                    connector.queryObject(JmxConnectionManager.class).simulateConnectionAbort();
                                }
                                finally {
                                    context.ungetService(ref);
                                }
                        return "OK";
                    }
                });
                result.start();
                return result;
            }
            else return null;
        }

        /**
         * Gets logger associated with this service.
         *
         * @return The logger associated with this service.
         */
        @Override
        @Aggregation
        @SpecialUse
        public Logger getLogger() {
            return JmxConnector.getLoggerImpl();
        }
    }

    private static final class JmxMaintenanceServiceManager extends MaintenanceServiceManager<JmxMaintenanceService> {

        @Override
        protected JmxMaintenanceService createMaintenanceService(final RequiredService<?>... dependencies) throws Exception {
            return new JmxMaintenanceService();
        }
    }

    private static final class JmxConnectorFactory extends ManagedResourceConnectorFactory<JmxConnector>{
        private final AtomicLong instances = new AtomicLong(0L);

        @Override
        public JmxConnector createConnector(final String resourceName,
                                            final String connectionString,
                                            final Map<String, String> connectionOptions,
                                            final RequiredService<?>... dependencies) throws MalformedURLException {
            final JmxConnectorLimitations limitations = JmxConnectorLimitations.current();
            limitations.verifyMaxInstanceCount(instances.get());
            limitations.verifyServiceVersion();
            final JmxConnector result = new JmxConnector(connectionString, connectionOptions);
            instances.incrementAndGet();
            JmxConnectorHelpers.log(Level.INFO, String.format("JMX connector for resource %s instantiated", resourceName), null);
            return result;
        }

        /**
         * Releases all resources associated with the resource connector.
         * <p>
         * This method just calls {@link AutoCloseable#close()} implemented in the connector.
         * </p>
         *
         * @param connector The instance of the connector to dispose.
         * @throws Exception Unable to dispose resource connector instance.
         */
        @Override
        public void releaseConnector(final JmxConnector connector) throws Exception {
            try {
                super.releaseConnector(connector);
            }
            finally {
                instances.decrementAndGet();

            }
        }
    }

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SpecialUse
    public JmxConnectorBundleActivator() {
        super(JmxConnector.NAME,
                new JmxConnectorFactory(),
                new RequiredService<?>[]{JmxConnectorLimitations.licenseReader},
                new SupportConnectorServiceManager<?, ?>[]{
                        new ConfigurationEntityDescriptionManager<JmxConnectorConfigurationDescriptor>() {
                            @Override
                            protected JmxConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                                return new JmxConnectorConfigurationDescriptor();
                            }
                        },
                        new JmxMaintenanceServiceManager(),
                        new LicensingDescriptionServiceManager<>(JmxConnectorLimitations.class, JmxConnectorLimitations.fallbackFactory),
                        new SimpleDiscoveryServiceManager<JMXConnector>() {

                            @Override
                            protected JMXConnector createManagementInformationProvider(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws IOException {
                                return new JmxConnectionOptions(connectionString, connectionOptions).createConnection();
                            }

                            @Override
                            protected <T extends ManagedEntity> Collection<T> getManagementInformation(final Class<T> entityType,
                                                                                                       final JMXConnector connection,
                                                                                                       final RequiredService<?>... dependencies) throws JMException, IOException {
                                return JmxDiscoveryService.discover(connection, entityType);
                            }
                        }
                });
    }
}
