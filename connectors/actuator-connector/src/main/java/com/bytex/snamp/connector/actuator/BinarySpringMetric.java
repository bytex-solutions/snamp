package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.ArrayType;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class BinarySpringMetric extends SpringMetric<byte[]> {
    private static final long serialVersionUID = 2442757042514679119L;

    BinarySpringMetric(final String name, final AttributeDescriptor descriptor) {
        super(name, ArrayType.getPrimitiveArrayType(byte[].class), descriptor);
    }

    @Override
    byte[] getValue(final JsonNode valueNode) throws IOException {
        return valueNode.getBinaryValue();
    }
}
