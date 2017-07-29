package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.IntBuffer;

/**
 * Provides deserialization of {@link IntBuffer} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class IntBufferDeserializer extends AbstractBufferDeserializer<IntBuffer> {
    @Override
    protected IntBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final IntBuffer result = IntBuffer.allocate(input.size());
        for (final JsonNode node : input)
            result.put(node.asInt());
        return result;
    }
}
