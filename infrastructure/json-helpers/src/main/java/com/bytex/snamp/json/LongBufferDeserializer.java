package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.LongBuffer;

/**
 * Provides deserialization of {@link LongBuffer} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class LongBufferDeserializer extends AbstractBufferDeserializer<LongBuffer> {
    @Override
    protected LongBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final LongBuffer result = LongBuffer.allocate(input.size());
        for(final JsonNode node: input)
            result.put(node.asLong());
        return result;
    }
}
