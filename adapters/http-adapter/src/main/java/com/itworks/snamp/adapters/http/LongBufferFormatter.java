package com.itworks.snamp.adapters.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.LongBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LongBufferFormatter extends AbstractBufferFormatter<LongBuffer> {
    @Override
    protected LongBuffer deserialize(final JsonArray json) throws JsonParseException {
        final long[] result = new long[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsLong();
        return Buffers.wrap(result);
    }

    @Override
    protected JsonArray serialize(final LongBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
