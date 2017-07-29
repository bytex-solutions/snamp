package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DoubleSpringMetric extends SpringMetric<Double> {
    private static final long serialVersionUID = -1405672321701634801L;

    DoubleSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.DOUBLE, descriptor);
    }

    @Override
    Double getValue(final JsonNode valueNode) {
        return valueNode.getDoubleValue();
    }
}
