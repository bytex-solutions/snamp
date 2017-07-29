package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.DoubleBuffer;

/**
 * Provides serialization of {@link DoubleBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class DoubleBufferSerializer extends AbstractBufferSerializer<DoubleBuffer> {
    @Override
    protected void serialize(final DoubleBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
