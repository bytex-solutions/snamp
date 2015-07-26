package com.bytex.snamp.security.auth.login.json.spi;

import com.google.common.collect.Maps;
import com.google.gson.*;

import javax.security.auth.login.AppConfigurationEntry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AppConfigurationEntrySerializerDeserializer implements JsonSerializer<AppConfigurationEntry>, JsonDeserializer<AppConfigurationEntry> {
    private static final String CONTROL_FLAG_FIELD = "controlFlag";
    private static final String LOGIN_MODULE_FIELD = "loginModuleName";

    private static JsonElement toJsonElement(final Object value) {
        if (value instanceof Boolean)
            return new JsonPrimitive((Boolean) value);
        else if (value instanceof Number)
            return new JsonPrimitive((Number) value);
        else if (value instanceof String)
            return new JsonPrimitive((String) value);
        else if (value != null)
            return new JsonPrimitive(value.toString());
        else return JsonNull.INSTANCE;
    }

    private static Object fromJsonElement(final JsonPrimitive obj){
        if(obj.isBoolean())
            return obj.getAsBoolean();
        else if(obj.isNumber())
            return obj.getAsNumber();
        else return obj.getAsString();
    }

    private static Object fromJsonElement(final JsonElement obj){
        if(obj.isJsonNull())
            return null;
        else if(obj.isJsonPrimitive())
            return fromJsonElement(obj.getAsJsonPrimitive());
        else return obj.toString();
    }

    @Override
    public JsonObject serialize(final AppConfigurationEntry src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();
        result.add(CONTROL_FLAG_FIELD, context.serialize(src.getControlFlag()));
        result.add(LOGIN_MODULE_FIELD, new JsonPrimitive(src.getLoginModuleName()));
        for(final Map.Entry<String, ?> entry: src.getOptions().entrySet())
            result.add(entry.getKey(), toJsonElement(entry.getValue()));
        return result;
    }

    private static AppConfigurationEntry deserialize(final JsonObject json, final JsonDeserializationContext context) throws JsonParseException {
        if (json.has(LOGIN_MODULE_FIELD) && json.has(CONTROL_FLAG_FIELD))
            try {
                final String loginModuleName = json.remove(LOGIN_MODULE_FIELD).getAsString();
                final LoginModuleControlFlag controlFlag = context.deserialize(json.remove(CONTROL_FLAG_FIELD), LoginModuleControlFlag.class);
                final Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
                final Map<String, Object> options = Maps.newHashMapWithExpectedSize(entries.size());
                for (final Map.Entry<String, JsonElement> entry : json.entrySet())
                    options.put(entry.getKey(), fromJsonElement(entry.getValue()));
                return new AppConfigurationEntry(loginModuleName, controlFlag, options);
            } catch (final Exception e) {
                throw new JsonParseException(e);
            }
        return null;
    }

    @Override
    public AppConfigurationEntry deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonObject())
            return deserialize(json.getAsJsonObject(), context);
        else throw new JsonParseException("JSON Object expected");
    }
}
