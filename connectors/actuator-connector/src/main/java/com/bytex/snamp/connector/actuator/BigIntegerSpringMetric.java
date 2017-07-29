package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.SimpleType;
import java.math.BigInteger;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class BigIntegerSpringMetric extends SpringMetric<BigInteger> {
    private static final long serialVersionUID = 8373678764749481308L;

    BigIntegerSpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, SimpleType.BIGINTEGER, descriptor);
    }

    @Override
    BigInteger getValue(final JsonNode valueNode) {
        return valueNode.getBigIntegerValue();
    }
}
