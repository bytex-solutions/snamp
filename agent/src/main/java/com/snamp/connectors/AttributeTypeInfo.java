package com.snamp.connectors;

/**
 * Represents an information about attribute type.
 * <p>
 *     It is recommended to use {@link EntityTypeInfoFactory} for creating
 *     attribute types.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see EntityTypeInfoFactory
 */
public interface AttributeTypeInfo extends EntityTypeInfo {
    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     * @param source The type of the value that can be converted to the attribute value.
     * @param <T> The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    public <T> boolean canConvertFrom(final Class<T> source);
}
