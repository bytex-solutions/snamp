package com.itworks.snamp.security.auth.login.spi;

import com.google.gson.*;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.security.auth.login.JsonConfiguration;

import javax.security.auth.login.AppConfigurationEntry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JsonConfigurationSerializerDeserializer implements JsonSerializer<JsonConfiguration>, JsonDeserializer<JsonConfiguration> {

    @Override
    public JsonObject serialize(final JsonConfiguration src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();
        for(final String realmName: src.keySet())
            result.add(realmName, context.serialize(ArrayUtils.toArray(src.get(realmName), AppConfigurationEntry.class),
                    AppConfigurationEntry[].class));
        return result;
    }

    private static JsonConfiguration deserialize(final Set<Map.Entry<String, JsonElement>> entries,
                                                 final JsonDeserializationContext context) throws JsonParseException{
        final JsonConfiguration result = new JsonConfiguration(entries.size());
        try {
            for (final Map.Entry<String, JsonElement> entry : entries)
                result.putAll(entry.getKey(), context.<AppConfigurationEntry[]>deserialize(entry.getValue(), AppConfigurationEntry[].class));
        }
        catch (final Exception e){
            throw new JsonParseException(e);
        }
        return result;
    }

    private static JsonConfiguration deserialize(final JsonObject json, final JsonDeserializationContext context) throws JsonParseException{
        return deserialize(json.entrySet(), context);
    }

    @Override
    public JsonConfiguration deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonObject())
            return deserialize(json.getAsJsonObject(), context);
        else throw new JsonParseException("JSON Object expected");
    }
}
