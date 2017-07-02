package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.FloatBuffer;

/**
 * Provides serialization of {@link FloatBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FloatBufferSerializer extends AbstractBufferSerializer<FloatBuffer> {
    @Override
    protected void serialize(final FloatBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
