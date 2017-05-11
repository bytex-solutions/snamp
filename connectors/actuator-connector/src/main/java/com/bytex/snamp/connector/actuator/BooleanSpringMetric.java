package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class BooleanSpringMetric extends SpringMetric<Boolean> {
    private static final long serialVersionUID = -2960491978655406399L;

    BooleanSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.BOOLEAN, descriptor);
    }

    @Override
    Boolean getValue(final JsonNode valueNode) {
        return valueNode.getBooleanValue();
    }
}
