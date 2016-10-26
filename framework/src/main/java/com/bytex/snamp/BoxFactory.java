package com.bytex.snamp;

/**
 * Represents factory of simple containers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class BoxFactory {
    private BoxFactory(){
        throw new InstantiationError();
    }

    public static <T> Box<T> create(final T value){
        return new SimpleBox<>(value);
    }

    public static BooleanBox createForBoolean(final boolean value){
        return new MutableBoolean(value);
    }

    public static IntBox createForInt(final int value){
        return new MutableInteger(value);
    }
}
