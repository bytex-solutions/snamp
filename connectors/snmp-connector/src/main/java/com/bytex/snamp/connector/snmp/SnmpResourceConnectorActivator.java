package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.log.OSGiLogFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;


/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator<SnmpResourceConnector> {
    static {
        OSGiLogFactory.setup();
    }

    @SpecialUse
    public SnmpResourceConnectorActivator() {
        super(SnmpResourceConnectorActivator::createConnector,
                new RequiredService<?>[]{new SimpleDependency<>(ThreadPoolRepository.class)},
                new SupportConnectorServiceManager<?, ?>[]{
                        configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance),
                        discoveryService(SnmpResourceConnectorActivator::newDiscoveryService)
                });
    }

    private static Duration getDiscoveryTimeout(final AgentConfiguration configuration){
        if(configuration.getParameters().containsKey(AgentConfiguration.DISCOVERY_TIMEOUT_PROPERTY)){
            final String timeout = configuration.getParameters().get(AgentConfiguration.DISCOVERY_TIMEOUT_PROPERTY);
            return Duration.ofMillis(Long.parseLong(timeout));
        } else
            return Duration.ofSeconds(5);
    }

    private static SnmpResourceConnector createConnector(final String resourceName,
                                                 final String connectionString,
                                                 final Map<String, String> connectionOptions,
                                                 final RequiredService<?>... dependencies) throws IOException {
        @SuppressWarnings("unchecked")
        final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
        assert configManager != null;

        final SnmpResourceConnector result =
                new SnmpResourceConnector(resourceName,
                        connectionString,
                        connectionOptions,
                        configManager.transformConfiguration(SnmpResourceConnectorActivator::getDiscoveryTimeout));
        result.listen();
        return result;
    }

    private static SnmpDiscoveryService newDiscoveryService(final RequiredService<?>... dependencies) throws IOException {
        @SuppressWarnings("unchecked")
        final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
        assert configManager != null;
        return new SnmpDiscoveryService(configManager.transformConfiguration(SnmpResourceConnectorActivator::getDiscoveryTimeout));
    }
}
