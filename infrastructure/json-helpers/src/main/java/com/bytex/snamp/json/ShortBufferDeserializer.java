package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.ShortBuffer;

/**
 * Provides deserialization of {@link ShortBuffer} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ShortBufferDeserializer extends AbstractBufferDeserializer<ShortBuffer> {
    @Override
    protected ShortBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final ShortBuffer result = ShortBuffer.allocate(input.size());
        for (final JsonNode node : input)
            result.put((short) node.asInt());
        return result;
    }
}
