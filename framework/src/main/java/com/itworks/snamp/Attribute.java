package com.itworks.snamp;

import org.apache.commons.lang3.reflect.Typed;

/**
 * Represents typed attribute that can be used to store and retrieve typed data
 * from the registry.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Attribute<T> {
    /**
     * Gets type of the attribute value.
     * @return The type of the attribute value.
     */
    Typed<T> getType();

    /**
     * Gets default value of the attribute.
     * @return The default value of the attribute.
     */
    T getDefaultValue();
}
