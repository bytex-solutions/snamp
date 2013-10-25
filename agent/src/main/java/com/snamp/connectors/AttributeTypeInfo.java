package com.snamp.connectors;

/**
 * Represents an information about attribute type.
 * @author roman
 */
public interface AttributeTypeInfo {
    /**
     * Determines whether the attribute value can be converted into the specified type.
     * @param target The result of the conversion.
     * @param <T> The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    public <T> boolean canConvertTo(final Class<T> target);

    /**
     * Converts the attribute value to the specified type.
     * @param value The attribute value to convert.
     * @param target The type of the conversion result.
     * @param <T> Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    public <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException;

    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     * @param source The type of the value that can be converted to the attribute value.
     * @param <T> The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    public <T> boolean canConvertFrom(final Class<T> source);
}
