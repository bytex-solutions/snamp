package com.itworks.snamp.adapters.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.ByteBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ByteBufferFormatter extends AbstractBufferFormatter<ByteBuffer> {
    @Override
    protected ByteBuffer deserialize(final JsonArray json) throws JsonParseException {
        final byte[] result = new byte[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsByte();
        return Buffers.wrap(result);
    }

    @Override
    protected JsonArray serialize(final ByteBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
