package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.FutureThread;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.licensing.LicensingException;
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
@SuppressWarnings("UnusedDeclaration")
public final class JmxConnectorBundleActivator extends AbstractManagedResourceActivator<JmxConnector> {

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
        public Future<String> doAction(final String actionName, final String arguments, final Locale loc) {
            if(Objects.equals(actionName, JmxMaintenanceActions.SIMULATE_CONNECTION_ABORT.getName())){
                final BundleContext context = getBundleContextByObject(this);
                final Map<String, ServiceReference<ManagedResourceConnector<?>>> connectors = ManagedResourceConnectorClient.getConnectors(context);
                final FutureThread<String> result = new FutureThread<>(new Callable<String>() {
                    @Override
                    public final String call() throws IOException, InterruptedException{
                        for(final ServiceReference<ManagedResourceConnector<?>> ref: connectors.values())
                            if(Objects.equals(getConnectorType(ref), JmxConnector.NAME))
                                try{
                                    final ManagedResourceConnector<?> connector = context.getService(ref);
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

    private static final class JmxConnectorManager extends NotificationSupportManager<JmxConnector> {

        public JmxConnectorManager(final String targetName){
            super(targetName, JmxConnectorLimitations.licenseReader);
        }

        /**
         * Creates a new instance of the management connector that supports notifications.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws java.net.MalformedURLException Invalid JMX connection string.
         */
        @Override
        protected JmxConnector newNotificationSupport(final String connectionString,
                                                      final Map<String, String> connectionOptions,
                                                      final RequiredService<?>... dependencies) throws MalformedURLException {
            return new JmxConnector(connectionString, connectionOptions);
        }
    }

    private static final class ProvidedJmxConnectors extends ServiceFactories<JmxConnector> {

        @Override
        protected ConfigurationEntityDescriptionManager<?> createDescriptionServiceManager(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new ConfigurationEntityDescriptionManager<JmxConnectorConfigurationDescriptor>() {
                @Override
                protected JmxConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                    return new JmxConnectorConfigurationDescriptor();
                }
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        protected LicensingDescriptionServiceManager createLicenseServiceManager() {
            return new LicensingDescriptionServiceManager(JmxConnectorLimitations.class, JmxConnectorLimitations.fallbackFactory);
        }

        @Override
        protected DiscoveryServiceManager<?> createDiscoveryServiceManager(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new SimpleDiscoveryServiceManager<JMXConnector>() {

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

                /**
                 * Gets logger associated with discovery service.
                 *
                 * @return The logger associated with discovery service.
                 */
                @Override
                protected Logger getLogger() {
                    return JmxConnector.getLoggerImpl();
                }
            };
        }

        @Override
        protected MaintenanceServiceManager<?> createMaintenanceServiceManager(final RequiredService<?>... dependencies) {
            return new JmxMaintenanceServiceManager();
        }

        /**
         * Creates a new instance of the management connector factory.
         *
         * @param resourceName         The name of the managed resource.
         * @param instances            Count of already instantiated connectors.
         * @param services             A collection of resolved dependencies.
         * @param activationProperties A collection of activation properties to read.
         * @return A new instance of the resource connector factory.
         */
        @Override
        protected ManagedResourceConnectorManager<JmxConnector> createConnectorManager(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            ManagedResourceConnectorManager<JmxConnector> result = null;
            try{
                final JmxConnectorLimitations limitations = JmxConnectorLimitations.current();
                limitations.verifyMaxInstanceCount(instances);
                limitations.verifyServiceVersion();
                result = new JmxConnectorManager(resourceName);
            }
            catch (final LicensingException e){
                JmxConnectorHelpers.log(Level.SEVERE, "The limit of instances is reached: %s. Unable to connect %s managed resource.", instances, resourceName, e);
            }
            return result;
        }
    }

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SuppressWarnings("UnusedDeclaration")
    public JmxConnectorBundleActivator() {
        super(JmxConnector.NAME,
                new ProvidedJmxConnectors());
    }

    /**
     * Adds global dependencies.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param dependencies A collection of connector's global dependencies.
     */
    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(JmxConnectorLimitations.licenseReader);
    }
}
