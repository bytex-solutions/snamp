package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.discovery.AbstractDiscoveryService;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;

/**
 * Represents SNMP discovery service.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpDiscoveryService extends AbstractDiscoveryService<SnmpClient> {
    private final Duration discoveryTimeout;

    SnmpDiscoveryService(final Duration discoveryTimeout){
        this.discoveryTimeout = Objects.requireNonNull(discoveryTimeout);
    }

    private static void setupAttributeOptions(final Variable v, final Map<String, String> options){
        if(v instanceof OctetString)
            options.put(SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat.adviceFormat((OctetString) v));
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final ClassLoader context,
                                                                         final SnmpClient client,
                                                                         final Duration discoveryTimeout) throws TimeoutException, InterruptedException, ExecutionException {
        return client.walk(discoveryTimeout).stream()
                .map(input ->{
                    final AttributeConfiguration config = ConfigurationManager.createEntityConfiguration(context, AttributeConfiguration.class);
                    if(config != null) {
                        config.setAlternativeName(input.getOid().toDottedString());
                        setupAttributeOptions(input.getVariable(), config.getParameters());
                    }
                    return config;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static  <T extends FeatureConfiguration> Collection<T> discover(final ClassLoader context,
                                                                            final Class<T> entityType,
                                                                            final SnmpClient client,
                                                                            final Duration discoveryTimeout) throws TimeoutException, InterruptedException, ExecutionException {
        if (Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>) discoverAttributes(context, client, discoveryTimeout);
        else return Collections.emptyList();
    }

    @Override
    public Logger getLogger() {
        return AbstractManagedResourceConnector.getLogger(SnmpResourceConnector.class);
    }

    @Override
    protected SnmpClient createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException {
        final SnmpClient client = SnmpConnectorDescriptionProvider.getInstance().createSnmpClient(GenericAddress.parse(connectionString), connectionOptions);
        client.listen();
        return client;
    }

    @Override
    protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final SnmpClient client) throws InterruptedException, ExecutionException, TimeoutException {
        return discover(getClass().getClassLoader(), entityType, client, discoveryTimeout);
    }
}
