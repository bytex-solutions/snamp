package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.DiscoveryService;
import com.itworks.snamp.licensing.LicensingException;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("UnusedDeclaration")
public final class JmxConnectorBundleActivator extends AbstractManagedResourceActivator<JmxConnector> {
    private static final class JmxConnectorProvider extends NotificationSupportProvider<JmxConnector>{

        public JmxConnectorProvider(final String targetName){
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

    private static final class ProvidedJmxConnectors extends ProvidedManagementConnectors<JmxConnector> {

        @Override
        protected ConfigurationEntityDescriptionProviderHolder<?> createDescriptionProvider(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new ConfigurationEntityDescriptionProviderHolder<JmxConnectorConfigurationDescriptor>() {
                @Override
                protected JmxConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                    return new JmxConnectorConfigurationDescriptor();
                }
            };
        }

        @Override
        protected DiscoveryServiceProvider<?> createDiscoveryServiceProvider(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new DiscoveryServiceProvider<DiscoveryService>() {
                @Override
                protected DiscoveryService createDiscoveryService(final RequiredService<?>... dependencies) throws Exception {
                    return new JmxDiscoveryService();
                }
            };
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
        protected ManagedResourceConnectorProvider<JmxConnector> createConnectorFactory(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            ManagedResourceConnectorProvider<JmxConnector> result = null;
            try{
                final JmxConnectorLimitations limitations = JmxConnectorLimitations.current();
                limitations.verifyMaxInstanceCount(instances);
                limitations.verifyServiceVersion();
                result = new JmxConnectorProvider(resourceName);
            }
            catch (final LicensingException e){
                JmxConnectorHelpers.getLogger().log(Level.SEVERE, String.format("The limit of instances is reached: %s. Unable to connect %s managed resource.", instances, resourceName), e);
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
                new ProvidedJmxConnectors(),
                JmxConnectorHelpers.getLogger());
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
