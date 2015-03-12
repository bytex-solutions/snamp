package com.itworks.snamp.jmx.json;

import com.google.gson.*;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ArrayOfTabularDataFormatter extends ArrayOfTabularDataSerializer implements JsonDeserializer<TabularData[]> {
    private final TabularType elementType;

    public ArrayOfTabularDataFormatter(final TabularType elementType) {
        this.elementType = Objects.requireNonNull(elementType);
    }

    public ArrayOfTabularDataFormatter(final ArrayType<TabularData> arrayType) {
        this((TabularType) arrayType.getElementOpenType());
    }

    static TabularData[] deserialize(final TabularType elementType,
                                     final JsonArray json,
                                     final JsonDeserializationContext context) {
        final TabularData[] result = new TabularData[json.size()];
        for (int i = 0; i < json.size(); i++)
            result[i] = CompositeDataFormatter.deserialize(elementType, json.get(i).getAsJsonArray(), context);
        return result;
    }

    @Override
    public TabularData[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray())
            return deserialize(elementType, json.getAsJsonArray(), context);
        else throw new JsonParseException("JSON Array expected");
    }
}