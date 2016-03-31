package com.bytex.snamp;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.Objects;

/**
 * Casts an object to the specified type.
 * This class cannot be inherited.
 * @param <O> Type of the conversion result.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
final class CastFunction<O> implements Function<Object, O>, Serializable {
    private static final long serialVersionUID = -4293792455315486544L;
    private final Class<O> type;

    private CastFunction(final Class<O> t){
        this.type = Objects.requireNonNull(t);
    }

    static <O> CastFunction<O> of(final Class<O> targetType){
        return new CastFunction<>(targetType);
    }

    /**
     * Casts input value.
     * @param input A value to convert.
     * @return Conversion result.
     */
    @Override
    public O apply(final Object input) {
        return type.cast(input);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    private boolean equals(final CastFunction<?> fn){
        return type.equals(fn.type);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof CastFunction<?> && equals((CastFunction<?>) obj);
    }

    @Override
    public String toString() {
        return String.format("typecast<%s>", type);
    }
}
