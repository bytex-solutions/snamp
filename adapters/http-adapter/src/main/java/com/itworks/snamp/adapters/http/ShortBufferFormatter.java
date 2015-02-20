package com.itworks.snamp.adapters.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.ShortBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ShortBufferFormatter extends AbstractBufferFormatter<ShortBuffer> {
    @Override
    protected ShortBuffer deserialize(final JsonArray json) throws JsonParseException {
        final short[] result = new short[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsShort();
        return Buffers.wrap(result);
    }

    @Override
    protected JsonArray serialize(final ShortBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
