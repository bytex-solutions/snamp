package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class IntegerSpringMetric extends SpringMetric<Long> {
    private static final long serialVersionUID = -5782496745998438494L;

    IntegerSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.LONG, descriptor);
    }

    @Override
    Long getValue(final JsonNode valueNode) {
        return valueNode.getLongValue();
    }
}
