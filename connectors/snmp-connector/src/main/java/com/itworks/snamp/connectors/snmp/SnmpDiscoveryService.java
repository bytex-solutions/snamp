package com.itworks.snamp.connectors.snmp;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.configuration.SerializableAgentConfiguration;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT_PARAM;

/**
 * Represents SNMP discovery service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpDiscoveryService {
    private static final String DISCOVERY_TIMEOUT_PROPERTY = "com.itworks.snamp.connectors.snmp.discoveryTimeout";

    private SnmpDiscoveryService(){

    }

    private static TimeSpan getDiscoveryTimeout(){
        return new TimeSpan(Long.valueOf(System.getProperty(DISCOVERY_TIMEOUT_PROPERTY, "5000")));
    }

    private static void setupAttributeOptions(final Variable v, final Map<String, String> options){
        if(v instanceof OctetString)
            options.put(SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat.adviceFormat((OctetString) v));
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final SnmpClient client) throws TimeoutException, InterruptedException, ExecutionException {
        return Collections2.transform(client.walk(getDiscoveryTimeout()), new Function<VariableBinding, AttributeConfiguration>() {
            @Override
            public AttributeConfiguration apply(final VariableBinding input) {
                final SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration config = new SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration();
                config.setAttributeName(input.getOid().toDottedString());
                setupAttributeOptions(input.getVariable(), config.getParameters());
                return config;
            }
        });
    }

    @SuppressWarnings("unchecked")
    static  <T extends ManagedEntity> Collection<T> discover(final Class<T> entityType, final SnmpClient client) throws TimeoutException, InterruptedException, ExecutionException {
        if (Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>) discoverAttributes(client);
        else return Collections.emptyList();
    }
}
