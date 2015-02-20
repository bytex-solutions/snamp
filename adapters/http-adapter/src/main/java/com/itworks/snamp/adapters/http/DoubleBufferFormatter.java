package com.itworks.snamp.adapters.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.DoubleBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DoubleBufferFormatter extends AbstractBufferFormatter<DoubleBuffer> {
    @Override
    protected DoubleBuffer deserialize(final JsonArray json) throws JsonParseException {
        final double[] result = new double[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsDouble();
        return Buffers.wrap(result);
    }

    @Override
    protected JsonArray serialize(final DoubleBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
