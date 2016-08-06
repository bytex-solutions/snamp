package com.bytex.snamp.jmx.json;

import com.google.gson.*;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import java.nio.*;
import java.util.Arrays;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class JsonUtils {
    private JsonUtils(){

    }

    /**
     * Registers all possible formatters for {@link Buffer} class and it derivatives.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder registerBufferAdapters(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(ByteBuffer.class, new ByteBufferFormatter())
                .registerTypeHierarchyAdapter(CharBuffer.class, new CharBufferFormatter())
                .registerTypeHierarchyAdapter(ShortBuffer.class, new ShortBufferFormatter())
                .registerTypeHierarchyAdapter(IntBuffer.class, new IntBufferFormatter())
                .registerTypeHierarchyAdapter(LongBuffer.class, new LongBufferFormatter())
                .registerTypeHierarchyAdapter(FloatBuffer.class, new FloatBufferFormatter())
                .registerTypeHierarchyAdapter(DoubleBuffer.class, new DoubleBufferFormatter());
    }

    /**
     * Registers GSON formatter for {@link CompositeData}, {@link TabularData} and {@link ObjectName} JMX-specific types.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder registerOpenTypeAdapters(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataFormatter())
                .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                .registerTypeHierarchyAdapter(TabularData.class, new TabularDataFormatter());
    }

    /**
     * Registers advanced formatter for {@link Notification} and {@link OpenType} (include it derivatives).
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder registerMiscJmxAdapters(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(Notification.class, new NotificationSerializer())
                .registerTypeHierarchyAdapter(OpenType.class, new OpenTypeFormatter());
    }

    /**
     * Registers all available serializers/deserializers in this library.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder registerTypeAdapters(final GsonBuilder builder){
        return registerBufferAdapters(registerOpenTypeAdapters(registerMiscJmxAdapters(builder)));
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
        return toJsonArray(Arrays.asList(values));
    }

    public static JsonArray toJsonArray(final boolean... values){
        final JsonArray result = new JsonArray();
        for(final boolean value: values)
            result.add(new JsonPrimitive(value));
        return result;
    }

    public static JsonArray toJsonArray(final Iterable<String> values){
        final JsonArray result = new JsonArray();
        for(final String value: values)
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

    public static JsonObject toJsonObject(final String name1, final JsonElement value1,
                                          final String name2, final JsonElement value2,
                                          final String name3, final JsonElement value3,
                                          final String name4, final JsonElement value4,
                                          final String name5, final JsonElement value5){
        final JsonObject result = toJsonObject(name1, value1,
                name2, value2,
                name3, value3,
                name4, value4);
        result.add(name5, value5);
        return result;
    }

    public static String[] parseStringArray(final JsonArray jsonArray) {
        final String[] result = new String[jsonArray.size()];
        for(int i = 0; i < jsonArray.size(); i++)
            result[i] = jsonArray.get(i).getAsString();
        return result;
    }

    public static byte[] parseByteArray(final JsonArray jsonArray) {
        final byte[] result = new byte[jsonArray.size()];
        for(int i = 0; i < jsonArray.size(); i++)
            result[i] = jsonArray.get(i).getAsByte();
        return result;
    }
}
