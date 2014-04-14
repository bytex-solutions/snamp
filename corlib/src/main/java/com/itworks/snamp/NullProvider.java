package com.itworks.snamp;

import org.apache.commons.collections4.Factory;


/**
 * Represents a factory which always produces {@literal null} reference.
 * @param <T> Type of the {@literal null} reference to produce.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NullProvider<T> implements Factory<T> {
    private NullProvider(){

    }

    @SuppressWarnings("unchecked")
    private static final NullProvider instance = new NullProvider<>();

    /**
     * Returns a factory which always produces {@literal null} reference for type <tt>T</tt>.
     * @param <T> Type of the factory.
     * @return An instance of the factory which always procuses {@literal null} reference.
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> get(){
        return instance;
    }

    /**
     * Always returns {@literal null}.
     * @return {@literal null} reference.
     */
    @Override
    public T create() {
        return null;
    }
}
