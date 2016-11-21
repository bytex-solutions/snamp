package com.bytex.snamp.webconsole.model.dto;


/**
 * The type Abstract dto class.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractDTOClass <T> {
    /**
     * Build abstract dto class.
     *
     * @param object the object
     * @return the abstract dto class
     */
    public abstract AbstractDTOClass<T> build(T object);
}
