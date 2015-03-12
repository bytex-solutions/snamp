package com.itworks.snamp.jmx.json;

import com.google.gson.*;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.lang.reflect.Type;

/**
 * Represents one-way converter from {@link javax.management.openmbean.CompositeData} to
 * JSON Object.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CompositeDataSerializer implements JsonSerializer<CompositeData> {

    static JsonObject serialize(final CompositeData src,
                                final JsonSerializationContext context){
        final JsonObject result = new JsonObject();
        for(final String itemName: src.getCompositeType().keySet()){
            final Object itemValue = src.get(itemName);
            if(itemValue instanceof CompositeData)
                result.add(itemName, serialize((CompositeData)itemValue, context));
            else if(itemValue instanceof TabularData)
                result.add(itemName, serialize((TabularData)itemValue, context));
            else result.add(itemName, context.serialize(itemValue));
        }
        return result;
    }

    /**
     * Serializes {@link javax.management.openmbean.CompositeData} into JSON Object.
     * @param src Composite data to convert.
     * @param typeOfSrc Type of the object to convert. May be {@literal null}.
     * @param context Serialization context.
     * @return JSON Object that represents {@link javax.management.openmbean.CompositeData}.
     */
    @Override
    public final JsonObject serialize(final CompositeData src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src, context);
    }

    static JsonArray serialize(final TabularData src, final JsonSerializationContext context) throws JsonParseException{
        final JsonArray result = new JsonArray();
        for(final Object row: src.values())
            if(row instanceof CompositeData)
                result.add(serialize((CompositeData)row, context));
        return result;
    }
}
