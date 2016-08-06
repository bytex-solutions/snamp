package com.bytex.snamp.connectors.snmp;

import com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration.FeatureConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.bytex.snamp.connectors.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;

/**
 * Represents SNMP discovery service.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpDiscoveryService {
    private SnmpDiscoveryService(){

    }

    private static void setupAttributeOptions(final Variable v, final Map<String, String> options){
        if(v instanceof OctetString)
            options.put(SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat.adviceFormat((OctetString) v));
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final ClassLoader context, final SnmpClient client) throws TimeoutException, InterruptedException, ExecutionException {
        return client.walk(SnmpConnectorHelpers.getDiscoveryTimeout()).stream()
                .map(input ->{
                    final AttributeConfiguration config = ConfigurationManager.createEntityConfiguration(context, AttributeConfiguration.class);
                    if(config != null) {
                        config.setAlternativeName(input.getOid().toDottedString());
                        setupAttributeOptions(input.getVariable(), config.getParameters());
                    }
                    return config;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @SuppressWarnings("unchecked")
    static  <T extends FeatureConfiguration> Collection<T> discover(final ClassLoader context, final Class<T> entityType, final SnmpClient client) throws TimeoutException, InterruptedException, ExecutionException {
        if (Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>) discoverAttributes(context, client);
        else return Collections.emptyList();
    }
}
