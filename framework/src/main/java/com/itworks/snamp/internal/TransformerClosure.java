package com.itworks.snamp.internal;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.itworks.snamp.Box;
import com.itworks.snamp.SafeConsumer;

import java.util.Objects;

/**
 * Represents a bridge between {@link com.google.common.base.Function} and {@link com.itworks.snamp.SafeConsumer}.
 * <p>
 *     Usually, this class can be used to obtain return value from the {@link com.itworks.snamp.SafeConsumer}
 *     functional interface invocation.
 * </p>
 * @param <I> Type of the value to transform.
 * @param <O> The transformation value.
 */
public abstract class TransformerClosure<I, O> implements SafeConsumer<I>, Function<I, O>, Supplier<O> {
    private final Box<O> valueHolder;

    protected TransformerClosure(final Box<O> valueHolder) {
        this.valueHolder = Objects.requireNonNull(valueHolder);
    }

    protected TransformerClosure() {
        this(new Box<O>());
    }

    protected TransformerClosure(final O defaultValue) {
        this(new Box<>(defaultValue));
    }

    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    @Override
    public final O get() {
        return valueHolder.get();
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    public final void accept(final I value) {
        valueHolder.set(value, this);
    }
}
