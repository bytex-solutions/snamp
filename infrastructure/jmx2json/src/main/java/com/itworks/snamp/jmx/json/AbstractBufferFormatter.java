package com.itworks.snamp.jmx.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.Buffer;

/**
 * Represents abstract serializer/deserializer of NIO buffers.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractBufferFormatter<B extends Buffer> implements JsonSerializer<B>, JsonDeserializer<B> {
    AbstractBufferFormatter(){

    }

    protected abstract B deserialize(final JsonArray json) throws JsonParseException;

    @Override
    public final B deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonArray())
            return deserialize(json.getAsJsonArray());
        else throw new JsonParseException("JSON Array expected");
    }

    protected abstract JsonArray serialize(final B src);

    @Override
    public final JsonArray serialize(final B src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src);
    }
}
