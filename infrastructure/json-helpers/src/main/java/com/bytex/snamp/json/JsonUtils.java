package com.bytex.snamp.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ArrayNode;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import java.nio.*;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

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

        addSerializer(Duration.class, new DurationSerializer());
        addDeserializer(Duration.class, new DurationDeserializer());

        addSerializer(Range.class, new RangeSerializer());
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

    public static Optional<JsonNode> find(final ArrayNode array, final Predicate<? super JsonNode> filter){
        for(final JsonNode node: array)
            if(filter.test(node))
                return Optional.of(node);
        return Optional.empty();
    }

    public static boolean contains(final ArrayNode array, final JsonNode node){
        return find(array, node::equals).isPresent();
    }

    private static Map<String, ?> listToMap(final List<?> items){
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(items.size());
        for(int i = 0; i < items.size(); i++)
            result.put(Integer.toString(i), items.get(i));
        return result;
    }

    public static Map<String, String> toPlainMap(final Map<String, ?> json, final char delimiter) {
        final class PlainMap extends HashMap<String, String> {
            private static final long serialVersionUID = -8377138475217097216L;

            private PlainMap(final int capacity) {
                super(capacity);
            }

            private String newKey(final String parentKey,
                                  final Object key) {
                return isNullOrEmpty(parentKey) ? key.toString() : parentKey + delimiter + key;
            }

            private void collect(final String parentKey, final Map<?, ?> json) {
                for (final Entry<?, ?> entry : json.entrySet()) {
                    final String key = newKey(parentKey, entry.getKey());
                    if (entry.getValue() instanceof String)
                        put(key, (String) entry.getValue());
                    else if (entry.getValue() instanceof Map)
                        collect(key, (Map<?, ?>) entry.getValue());
                    else if (entry.getValue() instanceof List<?>)
                        collect(key, listToMap((List<?>) entry.getValue()));
                    else if (entry.getValue() != null)
                        put(key, entry.getValue().toString());
                }
            }

            private void collect(final Map<?, ?> json) {
                collect(null, json);
            }
        }

        if (json == null || json.isEmpty())
            return ImmutableMap.of();
        else {
            final PlainMap map = new PlainMap(json.size());
            map.collect(json);
            return map;
        }
    }

    public static void exportToMap(final JsonNode node, final Map<String, Object> output, final Predicate<String> fieldFilter) {
        node.getFields().forEachRemaining(field -> {
            if (fieldFilter.test(field.getKey()))
                output.put(field.getKey(), field.getValue());
        });
    }
}
