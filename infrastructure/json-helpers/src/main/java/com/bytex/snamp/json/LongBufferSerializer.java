package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.LongBuffer;

/**
 * Provides serialization of {@link LongBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LongBufferSerializer extends AbstractBufferSerializer<LongBuffer> {
    @Override
    protected void serialize(final LongBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
