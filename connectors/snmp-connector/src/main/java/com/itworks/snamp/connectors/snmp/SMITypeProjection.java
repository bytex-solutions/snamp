package com.itworks.snamp.connectors.snmp;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.TypeLiterals;
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
    private final TypeToken<J> javaType;
    private final TypeToken<V> smiType;

    protected SMITypeProjection(final TypeToken<V> smiType, final TypeToken<J> javaType){
        this.javaType = javaType;
        this.smiType = smiType;
    }

    /**
     * Returns the type for which this converter is instantiated.
     *
     * @return The type for which this converter is instantiated.
     */
    @Override
    public final TypeToken<J> getType() {
        return javaType;
    }

    /**
     * Determines whether the value of the specified type can be converted into {@code T}.
     *
     * @param source The type of the source value.
     * @return {@literal true}, if the value of the specified type can be converted into {@code T}; otherwise, {@literal false}.
     */
    @Override
    public final boolean canConvertFrom(final TypeToken<?> source) {
        return smiType.isAssignableFrom(source);
    }

    protected abstract J convertFrom(final V value) throws IllegalArgumentException;

    /**
     * Converts the value into an instance of {@code T} class.
     *
     * @param value The value to convert.
     * @return The value of the original type described by this converter.
     * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(com.google.common.reflect.TypeToken)} method.
     */
    @Override
    public final J convertFrom(final Object value) throws IllegalArgumentException {
        if(TypeLiterals.isInstance(value, smiType)) return convertFrom(TypeLiterals.cast(value, smiType));
        else throw new IllegalArgumentException(String.format("%s is not an instance of %s", value, smiType));
    }
}
