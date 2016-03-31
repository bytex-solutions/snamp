package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class JsonDataConverter {
    private static JsonObject serialize(final Gson formatter, final CompositeData value){
        final JsonObject result = new JsonObject();
        for (final String itemName : value.getCompositeType().keySet())
            result.add(itemName, serialize(formatter, value.get(itemName)));
        return result;
    }

    /**
     * Converts attribute value to JSON object.
     * @param formatter JSON formatter.
     * @param value Value to serialize.
     * @return JSON representation of the specified value.
     */
    static JsonElement serialize(final Gson formatter, final Object value){
        if(value == null) return JsonNull.INSTANCE;
        else if(value instanceof CompositeData)
            return serialize(formatter, (CompositeData)value);
        else return formatter.toJsonTree(value);
    }

    private static CompositeData deserialize(final Gson formatter,
                                             final CompositeType type,
                                             final Set<Map.Entry<String, JsonElement>> items) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(items.size());
        for (final Map.Entry<String, JsonElement> entry : items)
                result.put(entry.getKey(), deserialize(formatter, type.getType(entry.getKey()), entry.getValue()));
        return new CompositeDataSupport(type, result);
    }

    private static CompositeData deserialize(final Gson formatter, final CompositeType expectedType, final JsonObject value) throws OpenDataException {
        return deserialize(formatter, expectedType, value.entrySet());
    }

    static Object deserialize(final Gson formatter, final OpenType<?> expectedType, final JsonElement value) throws OpenDataException{
        if(expectedType instanceof SimpleType<?> || expectedType instanceof ArrayType<?>){
            final WellKnownType knownType = WellKnownType.getType(expectedType);
            if(knownType != null) return formatter.fromJson(value, knownType.getJavaType());
        }
        else if(expectedType instanceof CompositeType)
            return deserialize(formatter, (CompositeType)expectedType, value.getAsJsonObject());
        throw new OpenDataException("Unsupported type: " + expectedType);
    }
}
