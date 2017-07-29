package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.CharBuffer;

/**
 * Deserializes JSON into {@link CharBuffer}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class CharBufferDeserializer extends AbstractBufferDeserializer<CharBuffer> {
    @Override
    protected CharBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final CharBuffer result = CharBuffer.allocate(input.size());
        for (final JsonNode node : input)
            result.put(node.asText());
        return result;
    }
}
