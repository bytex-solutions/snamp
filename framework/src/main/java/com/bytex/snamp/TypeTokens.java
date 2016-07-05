package com.bytex.snamp;

import com.google.common.reflect.TypeToken;

/**
 * Provides helpers for working with {@link TypeToken} instances.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class TypeTokens {
    private TypeTokens(){
        throw new InstantiationError();
    }

    /**
     * Determines whether the specified object is an instance of the type described by token.
     * @param value An object to test.
     * @param target Token that describes a type.
     * @return {@literal true}, if the specified object is an instance of the type described by token; otherwise, {@literal false}.
     */
    public static boolean isInstance(final Object value, final TypeToken<?> target) {
        return value != null && target.isSupertypeOf(value.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object value, final TypeToken<T> target) throws ClassCastException {
        if (isInstance(value, target))
            return (T) value;
        else throw new ClassCastException(String.format("Unable to cast %s to %s", value, target));
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeCast(final Object value, final TypeToken<T> target){
        return isInstance(value, target) ? (T)value : null;
    }
}
