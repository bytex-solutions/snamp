package com.itworks.snamp.jmx.json;

import com.google.gson.*;

import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents serialization/deserialization engine for {@link TabularData}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TabularDataFormatter implements JsonDeserializer<TabularData>, JsonSerializer<TabularData> {
    private static final String TYPE_FIELD = "type";
    private static final String VALUE_FIELD = "rows";

    private static TabularData deserialize(final JsonObject json,
                                   final JsonDeserializationContext context) throws JsonParseException{
        final TabularType type = (TabularType)OpenTypeFormatter.deserialize(json.getAsJsonObject(TYPE_FIELD));
        return CompositeDataFormatter.deserialize(type, json.getAsJsonArray(VALUE_FIELD), context);
    }

    @Override
    public TabularData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonObject())
            return deserialize(json.getAsJsonObject(), context);
        else throw new JsonParseException("JSON Object expected");
    }

    private static JsonObject serialize(final TabularData src, final JsonSerializationContext context){
        final JsonObject result = new JsonObject();
        result.add(TYPE_FIELD, OpenTypeFormatter.serialize(src.getTabularType()));
        result.add(VALUE_FIELD, CompositeDataFormatter.serializeRows(src, context));
        return result;
    }

    @Override
    public JsonElement serialize(final TabularData src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src, context);
    }
}
