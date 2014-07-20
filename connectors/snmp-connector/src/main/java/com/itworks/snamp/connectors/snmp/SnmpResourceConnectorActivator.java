package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.licensing.LicensingException;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends AbstractManagedResourceActivator<SnmpResourceConnector> {

    private static final class SnmpManagedResourceConnectorProviderFactory extends ManagedResourceConnectorProviderFactory<SnmpResourceConnector> {

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
        protected ManagedResourceConnectorProvider<SnmpResourceConnector> createConnectorFactory(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            ManagedResourceConnectorProvider<SnmpResourceConnector> result = null;
            try{
                final SnmpConnectorLicenseLimitations limitations = SnmpConnectorLicenseLimitations.current();
                limitations.verifyMaxInstanceCount(instances);
                limitations.verifyServiceVersion();
                result = new ManagedResourceConnectorProvider<SnmpResourceConnector>(resourceName) {
                    @Override
                    protected SnmpResourceConnector createConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
                        if(SnmpConnectionOptions.authenticationRequred(connectionOptions))
                            limitations.verifyAuthenticationFeature();
                        return new SnmpResourceConnector(connectionString, connectionOptions);
                    }
                };
            }
            catch (final LicensingException e){
                SnmpConnectorHelpers.getLogger().log(Level.SEVERE, String.format("The limit of instances is reached: %s. Unable to connect %s managed resource.", instances, resourceName), e);
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
        protected ConfigurationEntityDescriptionProviderHolder<?> createDescriptionProvider(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new ConfigurationEntityDescriptionProviderHolder<SnmpConnectorConfigurationProvider>() {
                @Override
                protected SnmpConnectorConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
                    return new SnmpConnectorConfigurationProvider();
                }
            };
        }

        @Override
        protected LicensingDescriptionServiceProvider<SnmpConnectorLicenseLimitations> createLicenseLimitationsProvider() {
            return new LicensingDescriptionServiceProvider<>(SnmpConnectorLicenseLimitations.class, SnmpConnectorLicenseLimitations.fallbackFactory);
        }

        /**
         * Creates a new instance of the {@link com.itworks.snamp.connectors.DiscoveryService} factory.
         *
         * @param activationProperties    A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         * @return A new factory of the special service that can automatically discover elements of the managed resource.
         * @see com.itworks.snamp.connectors.DiscoveryService
         */
        @Override
        protected DiscoveryServiceProvider<?> createDiscoveryServiceProvider(final ActivationPropertyReader activationProperties, final RequiredService<?>... bundleLevelDependencies) {
            return new DiscoveryServiceProvider<SnmpDiscoveryService>() {
                @Override
                protected SnmpDiscoveryService createDiscoveryService(final RequiredService<?>... dependencies) throws Exception {
                    return new SnmpDiscoveryService();
                }
            };
        }
    }

    public SnmpResourceConnectorActivator(){
        super(SnmpConnectorHelpers.CONNECTOR_NAME, new SnmpManagedResourceConnectorProviderFactory());
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
