package com.itworks.snamp.adapters.http;

import com.google.gson.*;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.lang.reflect.Type;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
class CompositeDataJsonSerializer implements JsonSerializer<CompositeData> {

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
