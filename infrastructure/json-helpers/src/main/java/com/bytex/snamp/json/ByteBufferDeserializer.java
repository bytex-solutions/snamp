package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.ByteBuffer;

/**
 * Deserializes JSON into {@link ByteBuffer}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ByteBufferDeserializer extends AbstractBufferDeserializer<ByteBuffer> {
    @Override
    protected ByteBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final ByteBuffer result = ByteBuffer.allocate(input.size());
        for(final JsonNode node: input)
            result.put((byte) node.asInt());
        return result;
    }
}
