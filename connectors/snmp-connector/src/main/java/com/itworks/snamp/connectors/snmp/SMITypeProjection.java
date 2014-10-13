package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.TypeLiterals;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;
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
    private final Typed<J> javaType;
    private final Typed<V> smiType;

    protected SMITypeProjection(final Typed<V> smiType, final Typed<J> javaType){
        this.javaType = javaType;
        this.smiType = smiType;
    }

    /**
     * Returns the type for which this converter is instantiated.
     *
     * @return The type for which this converter is instantiated.
     */
    @Override
    public final Typed<J> getType() {
        return javaType;
    }

    /**
     * Determines whether the value of the specified type can be converted into {@code T}.
     *
     * @param source The type of the source value.
     * @return {@literal true}, if the value of the specified type can be converted into {@code T}; otherwise, {@literal false}.
     */
    @Override
    public final boolean canConvertFrom(final Typed<?> source) {
        return TypeUtils.isAssignable(source.getType(), smiType.getType());
    }

    protected abstract J convertFrom(final V value) throws IllegalArgumentException;

    /**
     * Converts the value into an instance of {@code T} class.
     *
     * @param value The value to convert.
     * @return The value of the original type described by this converter.
     * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(Typed)} method.
     */
    @Override
    public final J convertFrom(final Object value) throws IllegalArgumentException {
        if(TypeLiterals.isInstance(value, smiType)) return convertFrom(TypeLiterals.cast(value, smiType));
        else throw new IllegalArgumentException(String.format("%s is not an instance of %s", value, smiType));
    }
}
