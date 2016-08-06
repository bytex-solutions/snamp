package com.bytex.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.nio.IntBuffer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class IntBufferFormatter extends AbstractBufferFormatter<IntBuffer> {
    @Override
    protected IntBuffer deserialize(final JsonArray json) throws JsonParseException {
        final IntBuffer result = IntBuffer.allocate(json.size());
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
