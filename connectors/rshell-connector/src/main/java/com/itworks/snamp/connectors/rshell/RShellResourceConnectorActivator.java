package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.connectors.AbstractManagedResourceActivator;

import java.util.Map;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends AbstractManagedResourceActivator<RShellResourceConnector> {
    private static final class RShellResourceConnectorManager extends ManagedResourceConnectorManager<RShellResourceConnector> {

        /**
         * Initializes a new management connector factory.
         *
         * @param managedResource The name of the managed resource.
         * @throws IllegalArgumentException config is {@literal null}.
         */
        private RShellResourceConnectorManager(final String managedResource) {
            super(managedResource);
        }

        /**
         * Creates a new instance of the management connector.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        @Override
        protected RShellResourceConnector createConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
            return new RShellResourceConnector(connectionString, connectionOptions);
        }
    }

    private static final class RShellResourceConnectorFactory extends ServiceFactories<RShellResourceConnector> {

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
        protected ManagedResourceConnectorManager<RShellResourceConnector> createConnectorManager(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            return new RShellResourceConnectorManager(resourceName);
        }

        /**
         * Creates a new factory for the special service that provides configuration schema for the resource connector.
         * <p>
         * In the default implementation this method returns {@literal null}.
         * </p>
         *
         * @param activationProperties    A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         * @return A new factory for the special service that provides configuration schema for the resource connector.
         * @see com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider
         */
        @Override
        protected ConfigurationEntityDescriptionManager<RShellConnectorConfigurationDescriptor> createDescriptionServiceManager(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new ConfigurationEntityDescriptionManager<RShellConnectorConfigurationDescriptor>() {
                @Override
                protected RShellConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                    return new RShellConnectorConfigurationDescriptor();
                }
            };
        }
    }

    /**
     * Initializes a new instance of the connector activator.
     */
    public RShellResourceConnectorActivator() {
        super(RShellResourceConnector.NAME, new RShellResourceConnectorFactory());
    }
}
