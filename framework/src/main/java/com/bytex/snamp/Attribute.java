package com.bytex.snamp;

import com.google.common.reflect.TypeToken;

import java.io.Serializable;

/**
 * Represents typed attribute that can be used to store and retrieve typed data
 * from abstract registry.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface Attribute<T> extends Serializable {
    /**
     * Gets type of the attribute value.
     * @return The type of the attribute value.
     */
    TypeToken<T> getType();

    /**
     * Gets default value of the attribute.
     * @return The default value of the attribute.
     */
    T getDefaultValue();
}
