package com.bytex.snamp.jmx.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents JSON serialization context reconstructed from GSON serializer/deserializer.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class GsonSerializationContext implements JsonSerializationContext {
    private final Gson formatter;

    public GsonSerializationContext(final Gson gson){
        formatter = Objects.requireNonNull(gson);
    }

    /**
     * Invokes default serialization on the specified object.
     *
     * @param src the object that needs to be serialized.
     * @return a tree of {@link com.google.gson.JsonElement}s corresponding to the serialized form of {@code src}.
     */
    @Override
    public JsonElement serialize(final Object src) {
        return formatter.toJsonTree(src);
    }

    /**
     * Invokes default serialization on the specified object passing the specific type information.
     * It should never be invoked on the element received as a parameter of the
     * {@link com.google.gson.JsonSerializer#serialize(Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)} method. Doing
     * so will result in an infinite loop since Gson will in-turn call the custom serializer again.
     *
     * @param src       the object that needs to be serialized.
     * @param typeOfSrc the actual genericized type of src object.
     * @return a tree of {@link com.google.gson.JsonElement}s corresponding to the serialized form of {@code src}.
     */
    @Override
    public JsonElement serialize(final Object src, final Type typeOfSrc) {
        return formatter.toJsonTree(src, typeOfSrc);
    }
}
