package com.itworks.snamp.adapters.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.io.Buffers;

import java.nio.CharBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CharBufferFormatter extends AbstractBufferFormatter<CharBuffer> {
    @Override
    protected CharBuffer deserialize(final JsonArray json) throws JsonParseException {
        final char[] result = new char[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = json.get(i).getAsCharacter();
        return Buffers.wrap(result);
    }

    @Override
    protected JsonArray serialize(final CharBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
