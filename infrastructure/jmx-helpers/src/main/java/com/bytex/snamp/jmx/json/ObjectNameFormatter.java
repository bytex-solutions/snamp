package com.bytex.snamp.jmx.json;

import com.google.gson.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.Type;

/**
 * Represents converter from {@link javax.management.ObjectName} to JSON String
 * and vice versa.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ObjectNameFormatter implements JsonSerializer<ObjectName>, JsonDeserializer<ObjectName> {

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
    public ObjectName deserialize(final JsonElement json,
                                  final Type typeOfT,
                                  final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonPrimitive())
            return deserialize(json.getAsJsonPrimitive());
        else throw new JsonParseException("JSON String expected");
    }

    @Override
    public JsonPrimitive serialize(final ObjectName src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src);
    }
}
