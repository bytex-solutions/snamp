package com.itworks.snamp.jmx.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javax.management.openmbean.CompositeData;
import java.lang.reflect.Type;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ArrayOfCompositeDataSerializer implements JsonSerializer<CompositeData[]> {

    static JsonArray serialize(final CompositeData[] src,
                               final JsonSerializationContext context){
        final JsonArray result = new JsonArray();
        for(final CompositeData dict: src)
            result.add(CompositeDataSerializer.serialize(dict, context));
        return result;
    }

    @Override
    public JsonArray serialize(final CompositeData[] src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src, context);
    }
}
