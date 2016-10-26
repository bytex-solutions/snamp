package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.management.AbstractMaintainable;
import com.bytex.snamp.management.Maintainable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JmxConnectorActivator extends ManagedResourceActivator<JmxConnector> {

    private static final class JmxMaintenanceService extends AbstractAggregator implements Maintainable{

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
                            if(Objects.equals(ManagedResourceConnector.getResourceConnectorType(ref.getBundle()), JmxConnector.getType()))
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

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SpecialUse
    public JmxConnectorActivator() {
        super(JmxConnectorActivator::createConnector,
                configurationDescriptor(JmxConnectorDescriptionProvider::getInstance),
                maintenanceService(JmxMaintenanceService::new),
                discoveryService(JmxConnectorActivator::newDiscoveryService));
    }

    private static JmxConnector createConnector(final String resourceName,
                                        final String connectionString,
                                        final Map<String, String> connectionOptions,
                                        final RequiredService<?>... dependencies) throws MalformedURLException, MalformedObjectNameException {
        return new JmxConnector(resourceName, connectionString, connectionOptions);
    }

    private static JmxDiscoveryService newDiscoveryService(final RequiredService<?>... dependencies){
        return new JmxDiscoveryService();
    }
}
