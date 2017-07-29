package com.bytex.snamp.json;

import org.codehaus.jackson.node.ArrayNode;

import java.nio.CharBuffer;

/**
 * Provides serialization of {@link java.nio.CharBuffer} into JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class CharBufferSerializer extends AbstractBufferSerializer<CharBuffer> {
    @Override
    protected void serialize(final CharBuffer input, final ArrayNode output) {
        while (input.hasRemaining())
            output.add(String.valueOf(input.get()));
    }
}
