package com.snamp;

/**
 * Represents type converter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface TypeConverter<T> {

    /**
     * Returns the type for which this converter is instantiated.
     * @return The type for which this converter is instantiated.
     */
    public Class<T> getType();

    /**
     * Determines whether the value of the specified type can be converted into {@code T}.
     * @param source The type of the source value.
     * @return {@literal true}, if the value of the specified type can be converted into {@code T}; otherwise, {@literal false}.
     */
    public boolean canConvertFrom(final Class<?> source);

    /**
     * Converts the value into an instance of {@code T} class.
     * @param value The value to convert.
     * @return The value of the original type described by this converter.
     * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(Class)} method.
     */
    public T convertFrom(final Object value) throws IllegalArgumentException;
}
