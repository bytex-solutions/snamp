package com.snamp;

/**
 * Represents lazy instance creator.
 * @param <T> Type of the instance that can be created by activator.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface Activator<T> {
    /**
     * Creates a new instance of the specified type.
     * @return A new instance of the specified type.
     */
    public T newInstance();
}
