package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.IntBuffer;

/**
 * Provides serialization of {@link java.nio.IntBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class IntBufferSerializer extends AbstractBufferSerializer<IntBuffer> {
    @Override
    protected void serialize(final IntBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
