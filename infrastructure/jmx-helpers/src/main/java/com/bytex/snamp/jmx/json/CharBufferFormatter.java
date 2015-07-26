package com.bytex.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.nio.CharBuffer;

/**
 * Represents converter from {@link java.nio.CharBuffer} to JSON Array and vice versa.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CharBufferFormatter extends AbstractBufferFormatter<CharBuffer> {
    /**
     * Converts JSON Array into {@link java.nio.CharBuffer}.
     * @param json JSON Array to convert.
     * @return {@link java.nio.CharBuffer} instance.
     */
    @Override
    public CharBuffer deserialize(final JsonArray json) {
        final CharBuffer result = CharBuffer.allocate(json.size());
        for(int i = 0; i < json.size(); i++)
            result.put(json.get(i).getAsCharacter());
        result.rewind();
        return result;
    }

    /**
     * Converts {@link java.nio.CharBuffer} into JSON Array.
     * @param src JSON Array to convert.
     * @return JSON Array that contains all characters from the buffer.
     */
    @Override
    public JsonArray serialize(final CharBuffer src) {
        final JsonArray result = new JsonArray();
        while (src.hasRemaining())
            result.add(new JsonPrimitive(src.get()));
        return result;
    }
}
