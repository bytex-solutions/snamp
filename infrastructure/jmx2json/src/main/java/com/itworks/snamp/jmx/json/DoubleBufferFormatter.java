package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.DoubleBuffer;

/**
 * Represents converter from {@link java.nio.DoubleBuffer} to JSON Array and vice versa.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DoubleBufferFormatter extends AbstractBufferFormatter<DoubleBuffer> {
    /**
     * Converts JSON Array into {@link java.nio.DoubleBuffer}.
     * @param json JSON Array to convert.
     * @return {@link java.nio.DoubleBuffer} instance.
     */
    @Override
    public DoubleBuffer deserialize(final JsonArray json) {
        final double[] result = new double[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsDouble();
        return Buffers.wrap(result);
    }

    /**
     * Converts {@link java.nio.ByteBuffer} into JSON Array.
     * @param src JSON Array to convert.
     * @return JSON Array that contains all floating-point numbers from the buffer.
     */
    @Override
    public JsonArray serialize(final DoubleBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
