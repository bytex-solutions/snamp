package com.itworks.snamp.jmx.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TabularDataFormatter extends TabularDataSerializer implements JsonDeserializer<TabularData> {
    private final TabularType type;

    public TabularDataFormatter(final TabularType t){
        this.type = Objects.requireNonNull(t);
    }

    @Override
    public TabularData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonArray())
            return CompositeDataFormatter.deserialize(type, json.getAsJsonArray(), context);
        else throw new JsonParseException("JSON Array expected");
    }
}
