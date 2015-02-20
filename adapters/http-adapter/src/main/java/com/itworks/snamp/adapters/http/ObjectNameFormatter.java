package com.itworks.snamp.adapters.http;

import com.google.gson.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.Type;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ObjectNameFormatter implements JsonSerializer<ObjectName>, JsonDeserializer<ObjectName> {

    static ObjectName deserialize(final JsonPrimitive json) throws JsonParseException{
        try {
            return new ObjectName(json.getAsString());
        } catch (final MalformedObjectNameException e) {
            throw new JsonParseException(e);
        }
    }

    static JsonPrimitive serialize(final ObjectName src){
        return new JsonPrimitive(src.getCanonicalName());
    }

    @Override
    public ObjectName deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonPrimitive())
            return deserialize(json.getAsJsonPrimitive());
        else throw new JsonParseException("JSON String expected");
    }

    @Override
    public JsonElement serialize(final ObjectName src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src);
    }
}
