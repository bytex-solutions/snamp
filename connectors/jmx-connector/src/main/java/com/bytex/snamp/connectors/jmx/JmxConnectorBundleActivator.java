package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.AbstractMaintainable;
import com.bytex.snamp.management.Maintainable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class JmxConnectorBundleActivator extends ManagedResourceActivator<JmxConnector> {

    private static final class JmxMaintenanceService extends AbstractAggregator implements Maintainable{

        private JmxMaintenanceService(){
        }

        /**
         * Returns read-only map of maintenance actions.
         *
         * @return Read-only map of maintenance actions.
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
                final BundleContext context = getBundleContextOfObject(this);
                final Map<String, ServiceReference<ManagedResourceConnector>> connectors = ManagedResourceConnectorClient.getConnectors(context);
                final FutureThread<String> result = new FutureThread<>(new Callable<String>() {
                    @Override
                    public String call() throws IOException, InterruptedException{
                        for(final ServiceReference<ManagedResourceConnector> ref: connectors.values())
                            if(Objects.equals(getConnectorType(ref), JmxConnector.getType()))
                                try{
                                    final ManagedResourceConnector connector = context.getService(ref);
                                    connector.queryObject(JmxConnectionManager.class).simulateConnectionAbort();
                                }
                                finally {
                                    context.ungetService(ref);
                                }
                        return "OK";
                    }

                    @Override
                    public String toString() {
                        return actionName + "-jmxConnectorActionThread";
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

    private static final class JmxConnectorFactory extends ManagedResourceConnectorModeler<JmxConnector> {

        @Override
        public JmxConnector createConnector(final String resourceName,
                                            final String connectionString,
                                            final Map<String, String> connectionOptions,
                                            final RequiredService<?>... dependencies) throws MalformedURLException, MalformedObjectNameException {
            return new JmxConnector(resourceName, connectionString, connectionOptions);
        }

        @Override
        protected boolean addAttribute(final JmxConnector connector,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected boolean enableNotifications(final JmxConnector connector,
                                              final String category,
                                              final CompositeData options) {
            return connector.enableNotifications(category, options);
        }

        @Override
        protected boolean enableOperation(final JmxConnector connector,
                                       final String operationName,
                                       final TimeSpan invocationTimeout,
                                       final CompositeData options) {
            return connector.enableOperation(operationName, invocationTimeout, options);
        }

        @Override
        protected void retainAttributes(final JmxConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected void retainNotifications(final JmxConnector connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected void retainOperations(final JmxConnector connector, final Set<String> operations) {
            connector.disableOperationsExcept(operations);
        }

        @Override
        public void releaseConnector(final JmxConnector connector) throws Exception {
            connector.close();
        }
    }

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SpecialUse
    public JmxConnectorBundleActivator() {
        super(new JmxConnectorFactory(),
                new ConfigurationEntityDescriptionManager<JmxConnectorDescriptionProvider>() {
                    @Override
                    protected JmxConnectorDescriptionProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                        return JmxConnectorDescriptionProvider.getInstance();
                    }
                },
                new JmxMaintenanceServiceManager(),
                new SimpleDiscoveryServiceManager<JMXConnector>() {

                    @Override
                    protected JMXConnector createManagementInformationProvider(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws MalformedObjectNameException, IOException {
                        return new JmxConnectionOptions(connectionString, connectionOptions).createConnection();
                    }

                    @Override
                    protected <T extends FeatureConfiguration> Collection<T> getManagementInformation(final Class<T> entityType,
                                                                                               final JMXConnector connection,
                                                                                               final RequiredService<?>... dependencies) throws JMException, IOException {
                        return JmxDiscoveryService.discover(getClass().getClassLoader(), connection, entityType);
                    }
                });
    }
}
