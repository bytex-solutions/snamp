package com.itworks.snamp.jmx.json;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.openmbean.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * Represents converter from {@link javax.management.openmbean.CompositeData} to JSON Object
 * and vice versa. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CompositeDataFormatter extends CompositeDataSerializer implements JsonDeserializer<CompositeData> {
    private final CompositeType type;

    /**
     * Initializes a new formatter for {@link javax.management.openmbean.CompositeData}.
     * @param t The type of the composite data. Cannot be {@literal null}.
     */
    public CompositeDataFormatter(final CompositeType t){
        this.type = Objects.requireNonNull(t);
    }

    static CompositeData deserialize( final CompositeType type,
                                      final JsonObject json,
                                      final JsonDeserializationContext context) throws JsonParseException {
        final Map<String, Object> entries = Maps.newHashMapWithExpectedSize(json.entrySet().size());
        for (final String itemName : type.keySet())
            if (json.has(itemName)) {
                final JsonElement itemValue = json.get(itemName);
                final OpenType<?> itemType = type.getType(itemName);
                if (itemType instanceof CompositeType && itemValue.isJsonObject())
                    entries.put(itemName, deserialize((CompositeType) itemType, itemValue.getAsJsonObject(), context));
                else if (itemType instanceof TabularType && itemValue.isJsonArray())
                    entries.put(itemName, deserialize((TabularType) itemType, itemValue.getAsJsonArray(), context));
                else entries.put(itemName, context.deserialize(itemValue, WellKnownType.getType(itemType).getType()));
            }
        try {
            return new CompositeDataSupport(type, entries);
        } catch (final OpenDataException e) {
            throw new JsonParseException(e);
        }
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
            return deserialize(type, json.getAsJsonObject(), context);
        else throw new JsonParseException("JSON Object expected");
    }

    static TabularData deserialize(final TabularType type, final JsonArray json, final JsonDeserializationContext context) throws JsonParseException{
        final TabularDataSupport result = new TabularDataSupport(type);
        for(final JsonElement row: json)
            if(row.isJsonObject())
                result.put(CompositeDataFormatter.deserialize(type.getRowType(), row.getAsJsonObject(), context));
        return result;
    }
}
