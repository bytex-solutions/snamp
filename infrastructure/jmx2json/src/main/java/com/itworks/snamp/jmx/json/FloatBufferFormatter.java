package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.FloatBuffer;

/**
 * Represents converter from {@link java.nio.FloatBuffer} to JSON Array and vice versa.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FloatBufferFormatter extends AbstractBufferFormatter<FloatBuffer> {
    /**
     * Converts JSON Array into {@link java.nio.FloatBuffer}.
     * @param json JSON Array to convert.
     * @return {@link java.nio.FloatBuffer} instance.
     */
    @Override
    public FloatBuffer deserialize(final JsonArray json) {
        final float[] result = new float[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsFloat();
        return Buffers.wrap(result);
    }

    /**
     * Converts {@link java.nio.FloatBuffer} into JSON Array.
     * @param src JSON Array to convert.
     * @return JSON Array that contains all floating-point numbers from the buffer.
     */
    @Override
    public JsonArray serialize(final FloatBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
