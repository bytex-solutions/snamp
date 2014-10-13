package com.itworks.snamp;

import org.apache.commons.lang3.reflect.Typed;

/**
 * Represents a base class for building type converters.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface TypeConverterProvider {
    /**
     * Returns the converter for the specified type.
     * @param t The type for which the converter should be created.
     * @param <T> The type for which the converter should be created.
     * @return The converter for the specified type; or {@literal null}, if the converter for the specified type
     * is not supported.
     */
    @SuppressWarnings("UnusedDeclaration")
    <T> TypeConverter<T> getTypeConverter(final Typed<T> t);
}
