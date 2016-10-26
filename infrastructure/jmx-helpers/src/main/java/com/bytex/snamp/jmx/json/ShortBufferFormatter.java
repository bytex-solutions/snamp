package com.bytex.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.nio.ShortBuffer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ShortBufferFormatter extends AbstractBufferFormatter<ShortBuffer> {
    @Override
    public ShortBuffer deserialize(final JsonArray json) throws JsonParseException {
        final ShortBuffer result = ShortBuffer.allocate(json.size());
        for(int i = 0; i < json.size(); i++)
            result.put(json.get(i).getAsShort());
        result.rewind();
        return result;
    }

    @Override
    public JsonArray serialize(final ShortBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
