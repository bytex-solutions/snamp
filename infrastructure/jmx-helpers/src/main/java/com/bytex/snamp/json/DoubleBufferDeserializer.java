package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;

import java.nio.DoubleBuffer;

/**
 * Provides deserialization of {@link DoubleBuffer} from JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DoubleBufferDeserializer extends AbstractBufferDeserializer<DoubleBuffer> {
    @Override
    protected DoubleBuffer deserialize(final ArrayNode input) throws JsonProcessingException {
        final DoubleBuffer result = DoubleBuffer.allocate(input.size());
        for(final JsonNode node: input)
            result.put(node.asDouble());
        return result;
    }
}
