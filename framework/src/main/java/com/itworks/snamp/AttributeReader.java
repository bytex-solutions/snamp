package com.itworks.snamp;

/**
 * Represents attribute reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeReader {
    /**
     * Gets value of the attribute.
     * @param attributeDef The definition of the attribute.
     * @param <T> Type of the attribute.
     * @return The value of the attribute.
     */
    <T> T getValue(final Attribute<T> attributeDef);
}
