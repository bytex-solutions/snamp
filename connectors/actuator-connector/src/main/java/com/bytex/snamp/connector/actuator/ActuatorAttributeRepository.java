package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.OpenDataException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository of metrics provided by Spring Actuator.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ActuatorAttributeRepository extends AbstractAttributeRepository<SpringMetric> {
    private final WebResource metricsResource;

        ActuatorAttributeRepository(final String resourceName, final WebResource metricsResource) {
        super(resourceName, SpringMetric.class);
        this.metricsResource = metricsResource;
    }

    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        final JsonNode metrics = metricsResource.get(JsonNode.class);
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        metrics.getFields().forEachRemaining(field -> result.put(field.getKey(), createDescriptor()));
        return result;
    }

    private static Optional<SpringMetric<?>> connectAttribute(final String attributeName,
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
