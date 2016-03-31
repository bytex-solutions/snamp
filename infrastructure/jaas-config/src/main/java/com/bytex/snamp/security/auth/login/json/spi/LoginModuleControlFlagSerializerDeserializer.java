package com.bytex.snamp.security.auth.login.json.spi;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

import static javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class LoginModuleControlFlagSerializerDeserializer implements JsonSerializer<LoginModuleControlFlag>, JsonDeserializer<LoginModuleControlFlag> {

    private static final ImmutableMap<String, LoginModuleControlFlag> flags;

    private static boolean isPublicStatic(final int modifiers) {
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    private static boolean isPublicStatic(final Field fld) {
        return isPublicStatic(fld.getModifiers());
    }

    static {
        final ImmutableMap.Builder<String, LoginModuleControlFlag> builder = ImmutableMap.builder();
        final Class<? extends LoginModuleControlFlag> flagType = LoginModuleControlFlag.class;
        for (final Field fld : flagType.getDeclaredFields())
            if (isPublicStatic(fld) && flagType.isAssignableFrom(fld.getType()))
                try {
                    builder.put(fld.getName(), flagType.cast(fld.get(null)));
                } catch (final IllegalAccessException | ClassCastException e) {
                    throw new ExceptionInInitializerError(e);
                }

        flags = builder.build();
    }

    @Override
    public JsonElement serialize(final LoginModuleControlFlag src, final Type typeOfSrc, final JsonSerializationContext context) {
        for (final String name : flags.keySet())
            if (flags.get(name) == src)
                return new JsonPrimitive(name);
        return JsonNull.INSTANCE;
    }

    @Override
    public LoginModuleControlFlag deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException{
        try {
            for (final Map.Entry<String, LoginModuleControlFlag> entry : flags.entrySet())
                if (entry.getKey().equalsIgnoreCase(json.getAsString()))
                    return entry.getValue();
        }
        catch (final Exception e){
            throw new JsonParseException(e);
        }
        return null;
    }
}
