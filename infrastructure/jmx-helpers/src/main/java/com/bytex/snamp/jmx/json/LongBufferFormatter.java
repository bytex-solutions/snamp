package com.bytex.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.nio.LongBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LongBufferFormatter extends AbstractBufferFormatter<LongBuffer> {
    @Override
    public LongBuffer deserialize(final JsonArray json) throws JsonParseException {
        final LongBuffer result = LongBuffer.allocate(json.size());
        for(int i = 0; i < json.size(); i++)
            result.put(json.get(i).getAsLong());
        result.rewind();
        return result;
    }

    @Override
    public JsonArray serialize(final LongBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
