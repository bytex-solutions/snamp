package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonUtils {
    private JsonUtils(){

    }

    public static JsonArray toJsonArray(final JsonElement... values){
        final JsonArray result = new JsonArray();
        for(final JsonElement value: values)
            result.add(value);
        return result;
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

    public static JsonObject toJsonObject(final String name, final JsonElement value){
        final JsonObject result = new JsonObject();
        result.add(name, value);
        return result;
    }

    public static JsonObject toJsonObject(final String name1, final JsonElement value1,
                                          final String name2, final JsonElement value2){
        final JsonObject result = toJsonObject(name1, value1);
        result.add(name2, value2);
        return result;
    }

    public static JsonObject toJsonObject(final String name1, final JsonElement value1,
                                          final String name2, final JsonElement value2,
                                          final String name3, final JsonElement value3){
        final JsonObject result = toJsonObject(name1, value1,
                name2, value2);
        result.add(name3, value3);
        return result;
    }

    public static JsonObject toJsonObject(final String name1, final JsonElement value1,
                                          final String name2, final JsonElement value2,
                                          final String name3, final JsonElement value3,
                                          final String name4, final JsonElement value4){
        final JsonObject result = toJsonObject(name1, value1,
                name2, value2,
                name3, value3);
        result.add(name4, value4);
        return result;
    }
}
