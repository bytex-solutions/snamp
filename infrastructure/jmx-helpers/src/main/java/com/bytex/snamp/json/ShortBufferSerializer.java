package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.ShortBuffer;

/**
 * Provides serialization of {@link ShortBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ShortBufferSerializer extends AbstractBufferSerializer<ShortBuffer> {
    @Override
    protected void serialize(final ShortBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(input.get());
    }
}
