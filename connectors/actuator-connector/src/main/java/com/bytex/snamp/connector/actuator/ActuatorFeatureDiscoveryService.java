package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.discovery.AbstractFeatureDiscoveryService;
import org.codehaus.jackson.JsonNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorFeatureDiscoveryService extends AbstractFeatureDiscoveryService<ActuatorConnectionOptions> {
    @Override
    protected ActuatorConnectionOptions createProvider(final String connectionString, final Map<String, String> connectionOptions) throws URISyntaxException {
        return new ActuatorConnectionOptions(new URI(connectionString), connectionOptions);
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final ClassLoader context, final ActuatorConnectionOptions options) {
        final JsonNode metrics = options.getMetricsResource().get(JsonNode.class);
        final Collection<AttributeConfiguration> result = new LinkedList<>();
        metrics.getFields().forEachRemaining(field -> {
            final AttributeConfiguration config = ConfigurationManager.createEntityConfiguration(context, AttributeConfiguration.class);
            if (config != null) {
                config.setAlternativeName(field.getKey());
                result.add(config);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final ActuatorConnectionOptions options) {
        if(AttributeConfiguration.class.equals(entityType))
            return (Collection<T>) discoverAttributes(getClass().getClassLoader(), options);
        else
            return Collections.emptyList();
    }
}
