package com.itworks.snamp.jmx.json;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

/**
 * Represents a function used to serialize Java objects into JSON strings.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
public final class JsonSerializerFunction implements Function<Object, String> {
    private final Gson formatter;

    public JsonSerializerFunction(final Gson formatter){
        this.formatter = Objects.requireNonNull(formatter);
    }

    public JsonSerializerFunction(final GsonBuilder builder){
        this(builder.create());
    }

    @Override
    public String apply(final Object input) {
        return formatter.toJson(input);
    }
}
