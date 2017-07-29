package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.ByteBuffer;

/**
 * Provides serialization of {@link ByteBuffer} into JSON object.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ByteBufferSerializer extends AbstractBufferSerializer<ByteBuffer> {
    @Override
    protected void serialize(final ByteBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
