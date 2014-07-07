package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.TypeConverter;
import org.snmp4j.smi.Variable;

/**
 * Represents type projection for SMI type.
 * @param <V> SMI type
 * @param <J> Associated Java type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SMITypeProjection<V extends Variable, J> implements TypeConverter<J> {
    private final Class<J> javaType;
    private final Class<V> smiType;

    protected SMITypeProjection(final Class<V> smiType, final Class<J> javaType){
        this.javaType = javaType;
        this.smiType = smiType;
    }

    /**
     * Returns the type for which this converter is instantiated.
     *
     * @return The type for which this converter is instantiated.
     */
    @Override
    public final Class<J> getType() {
        return javaType;
    }

    /**
     * Determines whether the value of the specified type can be converted into {@code T}.
     *
     * @param source The type of the source value.
     * @return {@literal true}, if the value of the specified type can be converted into {@code T}; otherwise, {@literal false}.
     */
    @Override
    public final boolean canConvertFrom(final Class<?> source) {
        return smiType.isAssignableFrom(source);
    }

    protected abstract J convertFrom(final V value) throws IllegalArgumentException;

    /**
     * Converts the value into an instance of {@code T} class.
     *
     * @param value The value to convert.
     * @return The value of the original type described by this converter.
     * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(Class)} method.
     */
    @Override
    public final J convertFrom(final Object value) throws IllegalArgumentException {
        if(smiType.isInstance(value)) return convertFrom(smiType.cast(value));
        else throw new IllegalArgumentException(String.format("%s is not an instance of %s", value, smiType));
    }
}
