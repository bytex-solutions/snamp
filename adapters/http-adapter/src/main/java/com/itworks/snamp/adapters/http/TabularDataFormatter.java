package com.itworks.snamp.adapters.http;

import com.google.gson.*;

import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TabularDataFormatter extends TabularDataJsonSerializer implements JsonDeserializer<TabularData> {
    private final TabularType type;

    TabularDataFormatter(final TabularType t){
        this.type = Objects.requireNonNull(t);
    }

    @Override
    public TabularData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonArray())
            return CompositeDataFormatter.deserialize(type, json.getAsJsonArray(), context);
        else throw new JsonParseException("JSON Array expected");
    }
}
