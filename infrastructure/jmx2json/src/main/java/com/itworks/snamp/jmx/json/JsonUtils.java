package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonUtils {
    private JsonUtils(){

    }

    public static JsonArray toJsonArray(final byte... values){
        final JsonArray result = new JsonArray();
        for(final byte value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final short... values){
        final JsonArray result = new JsonArray();
        for(final short value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final int... values){
        final JsonArray result = new JsonArray();
        for(final int value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final long... values){
        final JsonArray result = new JsonArray();
        for(final long value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final char... values){
        final JsonArray result = new JsonArray();
        for(final char value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final String... values){
        final JsonArray result = new JsonArray();
        for(final String value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final boolean... values){
        final JsonArray result = new JsonArray();
        for(final boolean value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }
}
