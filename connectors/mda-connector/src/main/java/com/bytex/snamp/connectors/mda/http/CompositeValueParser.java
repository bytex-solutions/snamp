package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeValueParser implements HttpValueParser {
    private final CompositeData defaultValue;

    CompositeValueParser(final CompositeType type) throws OpenDataException {
        defaultValue = DefaultValues.get(type);
    }

    @Override
    public CompositeData getDefaultValue() {
        return defaultValue;
    }

    private static CompositeData deserialize(final Set<Map.Entry<String, JsonElement>> items,
                                             final Gson formatter,
                                             final CompositeType type) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(items.size());
        for (final Map.Entry<String, JsonElement> entry : items) {
            final WellKnownType itemType = WellKnownType.getType(type.getType(entry.getKey()));
            if (itemType != null)
                result.put(entry.getKey(), formatter.fromJson(entry.getValue(), itemType.getJavaType()));
        }
        return new CompositeDataSupport(type, result);
    }

    static CompositeData deserialize(final JsonObject items,
                                             final Gson formatter,
                                             final CompositeType type) throws OpenDataException {
        return deserialize(items.entrySet(), formatter, type);
    }

    @Override
    public CompositeData deserialize(final JsonElement value, final Gson formatter) throws OpenDataException {
        return value.isJsonObject() ?
                deserialize(value.getAsJsonObject(), formatter, defaultValue.getCompositeType()) :
                defaultValue;
    }

    private static JsonObject serialize(final CompositeData data, final Gson formatter) {
        final JsonObject result = new JsonObject();
        for (final String itemName : data.getCompositeType().keySet())
            result.add(itemName, formatter.toJsonTree(data.get(itemName)));
        return result;
    }

    @Override
    public JsonObject serialize(final Object value, final Gson formatter) {
        return value instanceof CompositeData ?
                serialize((CompositeData) value, formatter) :
                serialize(defaultValue, formatter);
    }
}
