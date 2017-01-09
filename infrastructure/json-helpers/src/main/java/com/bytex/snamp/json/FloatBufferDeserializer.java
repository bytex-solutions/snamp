package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.FloatBuffer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FloatBufferDeserializer extends AbstractBufferDeserializer<FloatBuffer> {
    @Override
    protected FloatBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final FloatBuffer result = FloatBuffer.allocate(input.size());
        for(final JsonNode node: input)
            result.put((float) node.asDouble());
        return result;
    }
}
