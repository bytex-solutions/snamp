package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.IntBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class IntBufferFormatter extends AbstractBufferFormatter<IntBuffer> {
    @Override
    protected IntBuffer deserialize(final JsonArray json) throws JsonParseException {
        final IntBuffer result = Buffers.allocIntBuffer(json.size(), false);
        for(int i = 0; i < json.size(); i++)
            result.put(json.get(i).getAsInt());
        result.rewind();
        return result;
    }

    @Override
    protected JsonArray serialize(final IntBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}