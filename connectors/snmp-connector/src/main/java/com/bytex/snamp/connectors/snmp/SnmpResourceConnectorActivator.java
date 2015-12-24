package com.bytex.snamp.connectors.snmp;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.SpecialUse;
import org.snmp4j.log.OSGiLogFactory;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator<SnmpResourceConnector> {
    static {
        OSGiLogFactory.setup();
    }

    private static final class SnmpConnectorFactory extends ManagedResourceConnectorModeler<SnmpResourceConnector> {

        @Override
        public SnmpResourceConnector createConnector(final String resourceName,
                                                     final String connectionString,
                                                     final Map<String, String> connectionOptions,
                                                     final RequiredService<?>... dependencies) throws IOException {
            final SnmpResourceConnector result =
                    new SnmpResourceConnector(resourceName, connectionString, connectionOptions);
            result.listen();
            return result;
        }
        @Override
        protected boolean addAttribute(final SnmpResourceConnector connector, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected void retainAttributes(final SnmpResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final SnmpResourceConnector connector, final String category, final CompositeData options) {
            return connector.enableNotifications(category, options);
        }

        @Override
        protected void retainNotifications(final SnmpResourceConnector connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected boolean enableOperation(final SnmpResourceConnector connector, final String operationName, final TimeSpan timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainOperations(final SnmpResourceConnector connector, final Set<String> operations) {
            //not supported
        }
    }

    @SpecialUse
    public SnmpResourceConnectorActivator(){
        super( new SnmpConnectorFactory(),
                new ConfigurationEntityDescriptionManager<SnmpConnectorConfigurationProvider>() {
                    @Override
                    protected SnmpConnectorConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
                        return new SnmpConnectorConfigurationProvider();
                    }
                },
                new SimpleDiscoveryServiceManager<SnmpClient>() {

                    @Override
                    protected SnmpClient createManagementInformationProvider(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
                        final SnmpClient client = new SnmpConnectionOptions(connectionString, connectionOptions).createSnmpClient();
                        client.listen();
                        return client;
                    }

                    @Override
                    protected <T extends FeatureConfiguration> Collection<T> getManagementInformation(final Class<T> entityType, final SnmpClient client, final RequiredService<?>... dependencies) throws Exception {
                        return SnmpDiscoveryService.discover(entityType, client);
                    }
                });
    }
}
