package com.bytex.snamp.jmx.json;

import com.google.common.collect.Maps;
import com.google.gson.*;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Represents converter from {@link javax.management.openmbean.CompositeData} to JSON Object
 * and vice versa. This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class CompositeDataFormatter implements JsonDeserializer<CompositeData>, JsonSerializer<CompositeData> {
    private static final String TYPE_FIELD = "type";
    private static final String VALUE_FIELD = "value";

    static TabularData deserialize(final TabularType type,
                                   final JsonArray rows,
                                   final JsonDeserializationContext context){
        final TabularData result = new TabularDataSupport(type);
        for(final JsonElement row: rows)
            result.put(deserialize(type.getRowType(), row.getAsJsonObject(), context));
        return result;
    }

    static CompositeData deserialize(final CompositeType type,
                                     final JsonObject json,
                                     final JsonDeserializationContext context) throws JsonParseException{
        final Map<String, Object> entries = Maps.newHashMapWithExpectedSize(json.entrySet().size());
        for (final String itemName : type.keySet())
            if (json.has(itemName)) {
                final JsonElement itemValue = json.get(itemName);
                final OpenType<?> itemType = type.getType(itemName);
                if(itemType instanceof CompositeType)
                    entries.put(itemName, deserialize((CompositeType)itemType, itemValue.getAsJsonObject(), context));
                else if(itemType instanceof TabularType)
                    entries.put(itemName, deserialize((TabularType)itemType, itemValue.getAsJsonArray(), context));
                try {
                    entries.put(itemName, context.deserialize(itemValue, getJavaType(itemType)));
                } catch (final ClassNotFoundException e) {
                    throw new JsonParseException(e);
                }
            }
        try {
            return new CompositeDataSupport(type, entries);
        } catch (final OpenDataException e) {
            throw new JsonParseException(e);
        }
    }

    private static CompositeData deserialize(final JsonObject json,
                                      final JsonDeserializationContext context) throws JsonParseException {
        final CompositeType type = (CompositeType)OpenTypeFormatter.deserialize(json.getAsJsonObject(TYPE_FIELD));
        return deserialize(type, json.getAsJsonObject(VALUE_FIELD), context);
    }

    private static Type getJavaType(final OpenType<?> openType) throws ClassNotFoundException {
        if(openType instanceof CompositeType)
            return CompositeData.class;
        else if(openType instanceof TabularType)
            return TabularData.class;
        else if(Objects.equals(openType, SimpleType.BIGDECIMAL))
            return BigDecimal.class;
        else if(Objects.equals(openType, SimpleType.BIGINTEGER))
            return BigInteger.class;
        else if(Objects.equals(openType, SimpleType.DATE))
            return Date.class;
        else if(Objects.equals(openType, SimpleType.BOOLEAN))
            return Boolean.class;
        else if(Objects.equals(openType, SimpleType.BYTE))
            return Byte.class;
        else if(Objects.equals(openType, SimpleType.SHORT))
            return Short.class;
        else if(Objects.equals(openType, SimpleType.INTEGER))
            return Integer.class;
        else if(Objects.equals(openType, SimpleType.LONG))
            return Long.class;
        else if(Objects.equals(openType, SimpleType.OBJECTNAME))
            return ObjectName.class;
        else if(Objects.equals(openType, SimpleType.VOID))
            return Void.class;
        else if(Objects.equals(openType, SimpleType.CHARACTER))
            return Character.class;
        else if(Objects.equals(openType, SimpleType.STRING))
            return String.class;
        else if(Objects.equals(openType, SimpleType.FLOAT))
            return Float.class;
        else if(Objects.equals(openType, SimpleType.DOUBLE))
            return Double.class;
        else return Class.forName(openType.getClassName());
    }

    /**
     * Converts JSON Object to {@link javax.management.openmbean.CompositeData}.
     * @param json JSON Object to convert. Cannot be {@literal null}.
     * @param typeOfT {@link javax.management.openmbean.CompositeData}.class
     * @param context Deserialization context.
     * @return A new instance of the {@link javax.management.openmbean.CompositeData}.
     * @throws JsonParseException Unable convert JSON Object to {@link javax.management.openmbean.CompositeData}.
     */
    @Override
    public CompositeData deserialize(final JsonElement json,
                                     final Type typeOfT,
                                     final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonObject())
            return deserialize(json.getAsJsonObject(), context);
        else throw new JsonParseException("JSON Object expected");
    }

    @SuppressWarnings("unchecked")
    static JsonArray serializeRows(final TabularData src,
                                   final JsonSerializationContext context){
        final JsonArray rows = new JsonArray();
        for(final CompositeData row: (Iterable<? extends CompositeData>)src.values())
            rows.add(serializeFields(row, context));
        return rows;
    }

    static JsonObject serializeFields(final CompositeData src,
                                      final JsonSerializationContext context){
        final JsonObject result = new JsonObject();
        for(final String itemName: src.getCompositeType().keySet()){
            final Object itemValue = src.get(itemName);
            if(itemValue instanceof CompositeData)
                result.add(itemName, serializeFields((CompositeData) itemValue, context));
            else if(itemValue instanceof TabularData)
                result.add(itemName, serializeRows((TabularData)itemValue, context));
            else result.add(itemName, context.serialize(itemValue));
        }
        return result;
    }

    private static JsonObject serialize(final CompositeData src,
                                final JsonSerializationContext context){
        final JsonObject result = new JsonObject();
        result.add(TYPE_FIELD, OpenTypeFormatter.serialize(src.getCompositeType()));
        result.add(VALUE_FIELD, serializeFields(src, context));
        return result;
    }

    @Override
    public JsonElement serialize(final CompositeData src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src, context);
    }
}
