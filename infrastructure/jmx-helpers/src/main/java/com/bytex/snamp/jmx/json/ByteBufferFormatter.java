package com.bytex.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.nio.ByteBuffer;

/**
 * Represents converter from {@link java.nio.ByteBuffer} to JSON Array and vice versa.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ByteBufferFormatter extends AbstractBufferFormatter<ByteBuffer> {

    /**
     * Converts JSON Array into {@link java.nio.ByteBuffer}.
     * @param json JSON Array to convert.
     * @return {@link java.nio.ByteBuffer} instance.
     */
    @Override
    public ByteBuffer deserialize(final JsonArray json) {
        final ByteBuffer result = ByteBuffer.allocate(json.size());
        for(int i = 0; i < json.size(); i++)
            result.put(json.get(i).getAsByte());
        result.rewind();
        return result;
    }

    /**
     * Converts {@link java.nio.ByteBuffer} into JSON Array.
     * @param src JSON Array to convert.
     * @return JSON Array that contains all bytes from the buffer.
     */
    @Override
    public JsonArray serialize(final ByteBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
