package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.OpenDataException;
import java.util.*;

import static com.bytex.snamp.configuration.ConfigurationManager.createEntityConfiguration;

/**
 * Repository of metrics provided by Spring Actuator.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorAttributeRepository extends AbstractAttributeRepository<SpringMetric> {
    private final WebResource metricsResource;

    ActuatorAttributeRepository(final String resourceName, final WebResource metricsResource) {
        super(resourceName, SpringMetric.class, true);
        this.metricsResource = metricsResource;
    }

    private Collection<? extends SpringMetric<?>> expandAttributes(final JsonNode metrics) {
        final Collection<SpringMetric<?>> result = new LinkedList<>();
        metrics.getFields().forEachRemaining(field -> {
            final AttributeConfiguration config = createEntityConfiguration(getClass().getClassLoader(), AttributeConfiguration.class);
            assert config != null;
            config.setAutomaticallyAdded(true);
            config.setReadWriteTimeout(AttributeConfiguration.TIMEOUT_FOR_SMART_MODE);
            connectAttribute(field.getKey(), field.getValue(), new AttributeDescriptor(config)).ifPresent(result::add);
        });
        return result;
    }

    @Override
    public Collection<? extends SpringMetric<?>> expandAttributes() {
        final JsonNode node = metricsResource.get(JsonNode.class);
        if (node.isObject())
            try {
                return expandAttributes(node);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        return Collections.emptyList();
    }

    private Optional<SpringMetric<?>> connectAttribute(final String attributeName,
                                             final JsonNode valueNode,
                                             final AttributeDescriptor descriptor) {
        if (valueNode == null)
            return Optional.empty();
        else if (valueNode.isInt() || valueNode.isLong())
            return Optional.of(new IntegerSpringMetric(attributeName, descriptor));
        else if (valueNode.isTextual())
            return Optional.of(new TextSpringMetric(attributeName, descriptor));
        else if (valueNode.isDouble())
            return Optional.of(new DoubleSpringMetric(attributeName, descriptor));
        else if (valueNode.isBigInteger())
            return Optional.of(new BigIntegerSpringMetric(attributeName, descriptor));
        else if (valueNode.isBigDecimal())
            return Optional.of(new DecimalSpringMetric(attributeName, descriptor));
        else if (valueNode.isBoolean())
            return Optional.of(new BooleanSpringMetric(attributeName, descriptor));
        else if (valueNode.isBinary())
            return Optional.of(new BinarySpringMetric(attributeName, descriptor));
        else
            return Optional.empty();
    }

    @Override
    protected SpringMetric<?> connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final JsonNode node = metricsResource.get(JsonNode.class);
        final String metricName = descriptor.getAlternativeName().orElse(attributeName);
        if (node.isObject())
            return connectAttribute(attributeName, node.get(metricName), descriptor)
                    .orElseThrow(() -> new OpenDataException(String.format("Unsupported format of metric %s. JSON: %s", metricName, node)));
        else
            throw new OpenDataException(String.format("Unexpected metrics: %s", node));
    }

    @Override
    protected Object getAttribute(final SpringMetric metadata) throws Exception {
        return metadata.getValue(metricsResource);
    }

    @Override
    protected void setAttribute(final SpringMetric attribute, final Object value) throws Exception {
        throw new UnsupportedOperationException();
    }
}
