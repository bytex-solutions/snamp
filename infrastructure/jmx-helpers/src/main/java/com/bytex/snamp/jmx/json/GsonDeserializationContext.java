package com.bytex.snamp.jmx.json;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents deserialization context.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GsonDeserializationContext implements JsonDeserializationContext {
    private final Gson formatter;

    /**
     * Initializes a new deserialization context from the {@link com.google.gson.Gson} object.
     * @param gson A {@link com.google.gson.Gson} object used to create deserialization context.
     */
    public GsonDeserializationContext(final Gson gson){
        formatter = Objects.requireNonNull(gson);
    }

    /**
     * Invokes default deserialization on the specified object. It should never be invoked on
     * the element received as a parameter of the
     * {@link com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)} method. Doing
     * so will result in an infinite loop since Gson will in-turn call the custom deserializer again.
     *
     * @param json    the parse tree.
     * @param typeOfT type of the expected return value.
     * @return An object of type typeOfT.
     * @throws com.google.gson.JsonParseException if the parse tree does not contain expected data.
     */
    @Override
    public <T> T deserialize(final JsonElement json, final Type typeOfT) throws JsonParseException {
        return formatter.fromJson(json, typeOfT);
    }
}
