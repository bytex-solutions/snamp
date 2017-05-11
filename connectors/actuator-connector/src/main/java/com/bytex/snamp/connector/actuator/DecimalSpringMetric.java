package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DecimalSpringMetric extends SpringMetric<BigDecimal> {
    private static final long serialVersionUID = 55732541592454483L;

    DecimalSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.BIGDECIMAL, descriptor);
    }

    @Override
    BigDecimal getValue(final JsonNode valueNode) {
        return valueNode.getDecimalValue();
    }
}
