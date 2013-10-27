package com.snamp;

/**
 * Represents instance activator.
 * @author roman
 */
public interface Activator<T> {
    /**
     * Creates a new instance of the specified type.
     * @return A new instance of the specified type.
     */
    public T newInstance();
}
