package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class TextSpringMetric extends SpringMetric<String> {
    private static final long serialVersionUID = -2920809745665996732L;

    TextSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.STRING, descriptor);
    }

    @Override
    String getValue(final JsonNode valueNode) {
        return valueNode.getTextValue();
    }
}
