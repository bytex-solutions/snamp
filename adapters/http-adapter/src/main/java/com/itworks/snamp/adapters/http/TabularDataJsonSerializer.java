package com.itworks.snamp.adapters.http;

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
class TabularDataJsonSerializer implements JsonSerializer<TabularData> {
    @Override
    public JsonArray serialize(final TabularData src, final Type typeOfSrc, final JsonSerializationContext context) {
        return CompositeDataJsonSerializer.serialize(src, context);
    }
}
