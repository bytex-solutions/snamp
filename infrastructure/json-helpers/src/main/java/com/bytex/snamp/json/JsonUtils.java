package com.bytex.snamp.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ArrayNode;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.nio.*;
import java.util.Arrays;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JsonUtils extends SimpleModule {

    public JsonUtils() {
        super("SNAMP JMX-to-JSON Formatter", new Version(2, 0, 0, ""));
        addSerializer(ByteBuffer.class, new ByteBufferSerializer());
        addDeserializer(ByteBuffer.class, new ByteBufferDeserializer());

        addSerializer(CharBuffer.class, new CharBufferSerializer());
        addDeserializer(CharBuffer.class, new CharBufferDeserializer());

        addSerializer(ShortBuffer.class, new ShortBufferSerializer());
        addDeserializer(ShortBuffer.class, new ShortBufferDeserializer());

        addSerializer(IntBuffer.class, new IntBufferSerializer());
        addDeserializer(IntBuffer.class, new IntBufferDeserializer());

        addSerializer(LongBuffer.class, new LongBufferSerializer());
        addDeserializer(LongBuffer.class, new LongBufferDeserializer());

        addSerializer(FloatBuffer.class, new FloatBufferSerializer());
        addDeserializer(FloatBuffer.class, new FloatBufferDeserializer());

        addSerializer(DoubleBuffer.class, new DoubleBufferSerializer());
        addDeserializer(DoubleBuffer.class, new DoubleBufferDeserializer());

        addSerializer(ObjectName.class, new ObjectNameSerializer());
        addDeserializer(ObjectName.class, new ObjectNameDeserializer());

        addSerializer(Notification.class, new NotificationSerializer());

        addSerializer(OpenType.class, new OpenTypeSerializer());
        addDeserializer(OpenType.class, new OpenTypeDeserializer());

        addSerializer(CompositeData.class, new CompositeDataSerializer());
        addDeserializer(CompositeData.class, new CompositeDataDeserializer());

        addSerializer(TabularData.class, new TabularDataSerializer());
        addDeserializer(TabularData.class,  new TabularDataDeserializer());
    }

    public static ArrayNode toJsonArray(final JsonNode... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        result.addAll(Arrays.asList(values));
        return result;
    }

    public static ArrayNode toJsonArray(final byte... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final byte value: values)
            result.add(value);
        return result;
    }

    public static ArrayNode toJsonArray(final short... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final short value: values)
            result.add(value);
        return result;
    }

    public static ArrayNode toJsonArray(final int... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final int value: values)
            result.add(value);
        return result;
    }

    public static ArrayNode toJsonArray(final long... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final long value: values)
            result.add(value);
        return result;
    }

    public static ArrayNode toJsonArray(final char... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final char value: values)
            result.add(String.valueOf(value));
        return result;
    }

    public static ArrayNode toJsonArray(final String... values){
        return toJsonArray(Arrays.asList(values));
    }

    public static ArrayNode toJsonArray(final boolean... values){
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for(final boolean value: values)
            result.add(value);
        return result;
    }

    public static ArrayNode toJsonArray(final Iterable<String> values) {
        final ArrayNode result = ThreadLocalJsonFactory.getFactory().arrayNode();
        for (final String value : values)
            result.add(value);
        return result;
    }

    public static String[] toStringArray(final JsonNode jsonArray) {
        final String[] result = new String[jsonArray.size()];
        for(int i = 0; i < jsonArray.size(); i++)
            result[i] = jsonArray.get(i).asText();
        return result;
    }

    public static byte[] toByteArray(final JsonNode jsonArray) {
        final byte[] result = new byte[jsonArray.size()];
        for(int i = 0; i < jsonArray.size(); i++)
            result[i] = (byte) jsonArray.get(i).asInt();
        return result;
    }
}
