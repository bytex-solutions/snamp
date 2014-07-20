package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration.InMemoryAttributeConfiguration;
import com.itworks.snamp.connectors.DiscoveryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT;

/**
 * Represents SNMP discovery service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpDiscoveryService extends AbstractAggregator implements DiscoveryService {
    private static final String DISCOVERY_TIMEOUT_PROPERTY = "com.itworks.snamp.connectors.snmp.discoveryTimeout";

    private static TimeSpan getDiscoveryTimeout(){
        return new TimeSpan(Long.valueOf(System.getProperty(DISCOVERY_TIMEOUT_PROPERTY, "5000")));
    }

    private static void setupAttributeOptions(final Variable v, final Map<String, String> options){
        if(v instanceof OctetString)
            options.put(SNMP_CONVERSION_FORMAT, OctetStringConversionFormat.adviceFormat((OctetString)v).toString());
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final String connectionString, final Map<String, String> options){
        final SnmpConnectionOptions connection = new SnmpConnectionOptions(connectionString, options);
        try(final SnmpClient client = connection.createSnmpClient()){
            client.listen();
            return CollectionUtils.collect(client.walk(getDiscoveryTimeout()), new Transformer<VariableBinding, AttributeConfiguration>() {
                @Override
                public AttributeConfiguration transform(final VariableBinding input) {
                    final InMemoryAttributeConfiguration config = new InMemoryAttributeConfiguration();
                    config.setAttributeName(input.getOid().toDottedString());
                    setupAttributeOptions(input.getVariable(), config.getParameters());
                    return config;
                }
            });
        }
        catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ManagedEntity> Collection<T> discover(final String connectionString, final Map<String, String> connectionOptions, final Class<T> entityType) {
        if(Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>)discoverAttributes(connectionString, connectionOptions);
        else return Collections.emptyList();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return SnmpConnectorHelpers.getLogger();
    }
}
