package com.bytex.snamp.jmx.json;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

/**
 * Represents a function used to serialize Java objects into JSON strings.
 * This class cannot be inherited.
 * @param <I> Type of the object to be converted into JSON string.
 * @author Roman Sakno
 * @deprecated Use {@code Gson::toJson} method reference instead of this class.
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public final class JsonSerializerFunction<I> implements Function<I, String> {
    private final Gson formatter;

    public JsonSerializerFunction(final Gson formatter){
        this.formatter = Objects.requireNonNull(formatter);
    }

    public JsonSerializerFunction(final GsonBuilder builder){
        this(builder.create());
    }

    @Override
    public String apply(final I input) {
        return formatter.toJson(input);
    }
}
