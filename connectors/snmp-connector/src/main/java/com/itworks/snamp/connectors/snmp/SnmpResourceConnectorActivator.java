package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator<SnmpResourceConnector> {

    private static final class SnmpConnectorFactory extends ManagedResourceConnectorFactory<SnmpResourceConnector> {
        private final AtomicLong instances = new AtomicLong(0L);

        @Override
        public SnmpResourceConnector createConnector(final String resourceName,
                                                     final String connectionString,
                                                     final Map<String, String> connectionOptions,
                                                     final RequiredService<?>... dependencies) throws IOException {
            final SnmpConnectorLicenseLimitations limitations = SnmpConnectorLicenseLimitations.current();
            limitations.verifyMaxInstanceCount(instances.get());
            limitations.verifyServiceVersion();
            if (SnmpConnectionOptions.authenticationRequred(connectionOptions))
                limitations.verifyAuthenticationFeature();
            final SnmpResourceConnector result =
                    new SnmpResourceConnector(connectionString, connectionOptions);
            result.listen();
            return result;
        }
    }

    @SpecialUse
    public SnmpResourceConnectorActivator(){
        super(SnmpConnectorHelpers.CONNECTOR_NAME,
                new SnmpConnectorFactory(),
                new RequiredService<?>[]{SnmpConnectorLicenseLimitations.licenseReader},
                new SupportConnectorServiceManager<?, ?>[]{
                        new ConfigurationEntityDescriptionManager<SnmpConnectorConfigurationProvider>() {
                            @Override
                            protected SnmpConnectorConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
                                return new SnmpConnectorConfigurationProvider();
                            }
                        },
                        new LicensingDescriptionServiceManager<>(SnmpConnectorLicenseLimitations.class, SnmpConnectorLicenseLimitations.fallbackFactory),
                        new SimpleDiscoveryServiceManager<SnmpClient>() {

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
                        }
                });
    }
}
