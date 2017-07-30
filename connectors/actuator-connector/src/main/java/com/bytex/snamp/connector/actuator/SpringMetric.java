package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Represents single metric exposed by Spring Actuator.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
abstract class SpringMetric<T> extends AbstractOpenAttributeInfo {
    private static final long serialVersionUID = 5536078710775514254L;

    SpringMetric(final String name,
                 final OpenType<T> type,
                 final AttributeDescriptor descriptor) {
        super(name, type, "Spring Actuator metric", AttributeSpecifier.READ_ONLY, descriptor);
    }

    abstract T getValue(final JsonNode valueNode) throws Exception;

    final T getValue(final WebResource actuatorRoot) throws Exception{
        final JsonNode node = actuatorRoot.get(JsonNode.class);
        if (node.isObject()) {
            final JsonNode valueNode = node.get(name);
            if(valueNode.isValueNode())
                return getValue(valueNode);
            else
                throw new OpenDataException(String.format("'%s' is not a scalar value", valueNode));
        } else
            throw new OpenDataException(String.format("Unexpected metrics: %s", node));
    }
}
