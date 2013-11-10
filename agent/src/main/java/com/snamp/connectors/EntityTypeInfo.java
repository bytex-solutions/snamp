package com.snamp.connectors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface EntityTypeInfo {
    /**
     * Determines whether the value of the management entity can be converted into the specified type.
     * @param target The result of the conversion.
     * @param <T> The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    public <T> boolean canConvertTo(final Class<T> target);

    /**
     * Converts the value of the management entity to the specified type.
     * @param value The entity value to convert.
     * @param target The type of the conversion result.
     * @param <T> Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    public <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException;
}
