package com.itworks.snamp.jmx.json;

import com.google.gson.*;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ArrayOfCompositeDataFormatter extends ArrayOfCompositeDataSerializer implements JsonDeserializer<CompositeData[]> {
    private final CompositeType elementType;

    public ArrayOfCompositeDataFormatter(final CompositeType elementType){
        this.elementType = Objects.requireNonNull(elementType);
    }

    public ArrayOfCompositeDataFormatter(final ArrayType<CompositeData> arrayType){
        this((CompositeType)arrayType.getElementOpenType());
    }

    static CompositeData[] deserialize(final CompositeType elementType,
                                       final JsonArray json,
                                       final JsonDeserializationContext context){
        final CompositeData[] result = new CompositeData[json.size()];
        for(int i = 0; i < json.size(); i++)
            result[i] = CompositeDataFormatter.deserialize(elementType, json.get(i).getAsJsonObject(), context);
        return result;
    }

    @Override
    public CompositeData[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonArray())
            return deserialize(elementType, json.getAsJsonArray(), context);
        else throw new JsonParseException("JSON Array expected");
    }
}
