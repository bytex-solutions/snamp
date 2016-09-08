package com.bytex.snamp.concurrent;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * Represents time-constrained accumulator for {@code double} numbers.
 * @since 2.0
 * @version 2.0
 */
public abstract class TimeLimitedDouble extends TimeLimited implements DoubleSupplier, DoubleConsumer {
    private final AtomicDouble current;
    private final double initialValue;

    TimeLimitedDouble(final double initialValue, final LongSupplier ttl) {
        super(ttl);
        current = new AtomicDouble(this.initialValue = initialValue);
    }

    private void setInitialValue(){
        current.set(initialValue);
    }

    private void resetIfNecessary(){
        acceptIfExpired(this, TimeLimitedDouble::setInitialValue);
    }

    protected final double accumulateAndGet(final double value, final DoubleBinaryOperator operator){
        double prev, next;
        do{
            next = operator.applyAsDouble(prev = current.get(), value);
        } while (!current.compareAndSet(prev, next));
        return next;
    }

    protected abstract double accumulate(final double value);

    public final double update(final double value){
        resetIfNecessary();
        return accumulate(value);
    }

    @Override
    public void accept(final double value) {
        update(value);
    }

    @Override
    public double getAsDouble() {
        resetIfNecessary();
        return current.get();
    }

    public static TimeLimitedDouble create(final double initialValue, final long ttl, final DoubleBinaryOperator operator){
        return new TimeLimitedDouble(initialValue, () -> ttl) {
            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, operator);
            }
        };
    }

    public static TimeLimitedDouble peak(final double initialValue, final long ttl){
        return create(initialValue, ttl, Math::max);
    }

    public static TimeLimitedDouble min(final double initialValue, final long ttl){
        return create(initialValue, ttl, Math::min);
    }
}
