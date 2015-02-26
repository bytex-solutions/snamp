package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javax.management.openmbean.TabularData;
import java.lang.reflect.Type;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ArrayOfTabularDataSerializer implements JsonSerializer<TabularData[]> {

    static JsonArray serialize(final TabularData[] src,
                               final JsonSerializationContext context){
        final JsonArray result = new JsonArray();
        for(final TabularData table: src)
            result.add(CompositeDataSerializer.serialize(table, context));
        return result;
    }

    @Override
    public JsonArray serialize(final TabularData[] src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }
}
