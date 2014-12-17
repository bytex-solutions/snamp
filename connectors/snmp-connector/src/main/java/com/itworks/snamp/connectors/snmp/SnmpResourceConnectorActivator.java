package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.licensing.LicensingException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends AbstractManagedResourceActivator<SnmpResourceConnector> {

    private static final class SnmpServiceFactories extends ServiceFactories<SnmpResourceConnector> {

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
        protected ManagedResourceConnectorManager<SnmpResourceConnector> createConnectorManager(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            ManagedResourceConnectorManager<SnmpResourceConnector> result = null;
            try{
                final SnmpConnectorLicenseLimitations limitations = SnmpConnectorLicenseLimitations.current();
                limitations.verifyMaxInstanceCount(instances);
                limitations.verifyServiceVersion();
                result = new ManagedResourceConnectorManager<SnmpResourceConnector>(resourceName) {
                    @Override
                    protected SnmpResourceConnector createConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws IOException {
                        if(SnmpConnectionOptions.authenticationRequred(connectionOptions))
                            limitations.verifyAuthenticationFeature();
                        return new SnmpResourceConnector(connectionString, connectionOptions);
                    }
                };
            }
            catch (final LicensingException e){
                SnmpConnectorHelpers.log(Level.SEVERE, "The limit of instances is reached: %s. Unable to connect %s managed resource.", instances, resourceName, e);
            }
            return result;
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
        protected ConfigurationEntityDescriptionManager<?> createDescriptionServiceManager(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new ConfigurationEntityDescriptionManager<SnmpConnectorConfigurationProvider>() {
                @Override
                protected SnmpConnectorConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
                    return new SnmpConnectorConfigurationProvider();
                }
            };
        }

        @Override
        protected LicensingDescriptionServiceManager<SnmpConnectorLicenseLimitations> createLicenseServiceManager() {
            return new LicensingDescriptionServiceManager<>(SnmpConnectorLicenseLimitations.class, SnmpConnectorLicenseLimitations.fallbackFactory);
        }

        /**
         * Creates a new instance of the {@link com.itworks.snamp.connectors.discovery.DiscoveryService} factory.
         *
         * @param activationProperties    A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         * @return A new factory of the special service that can automatically discover elements of the managed resource.
         * @see com.itworks.snamp.connectors.discovery.DiscoveryService
         */
        @Override
        protected DiscoveryServiceManager<?> createDiscoveryServiceManager(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new SimpleDiscoveryServiceManager<SnmpClient>() {

                @Override
                protected SnmpClient createManagementInformationProvider(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
                    final SnmpClient client = new SnmpConnectionOptions(connectionString, connectionOptions).createSnmpClient();
                    client.listen();
                    return client;
                }

                @Override
                protected <T extends ManagedEntity> Collection<T> getManagementInformation(final Class<T> entityType, final SnmpClient client, final RequiredService<?>... dependencies) throws Exception {
                    return SnmpDiscoveryService.discover(entityType, client);
                }

                /**
                 * Gets logger associated with discovery service.
                 *
                 * @return The logger associated with discovery service.
                 */
                @Override
                protected Logger getLogger() {
                    return SnmpResourceConnector.getLoggerImpl();
                }
            };
        }
    }

    public SnmpResourceConnectorActivator(){
        super(SnmpConnectorHelpers.CONNECTOR_NAME, new SnmpServiceFactories());
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
        dependencies.add(SnmpConnectorLicenseLimitations.licenseReader);
    }
}
