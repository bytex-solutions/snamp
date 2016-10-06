package com.bytex.snamp.concurrent;

import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Represents time-constrained accumulator for {@code double} numbers.
 * @since 2.0
 * @version 2.0
 */
public abstract class TimeLimitedDouble extends TimeLimited implements DoubleSupplier, DoubleConsumer {
    private static final long serialVersionUID = 5932747666389586277L;
    private final AtomicDouble current;
    private final double initialValue;

    protected TimeLimitedDouble(final double initialValue, final Duration ttl) {
        super(ttl);
        current = new AtomicDouble(this.initialValue = initialValue);
    }

    private void setInitialValue(){
        current.set(initialValue);
    }

    private void resetIfNecessary(){
        acceptIfExpired(this, TimeLimitedDouble::setInitialValue);
    }

    protected final double addAndGet(final double value){
        return current.addAndGet(value);
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

    public static TimeLimitedDouble create(final double initialValue, final Duration ttl, final DoubleBinaryOperator operator){
        return new TimeLimitedDouble(initialValue, ttl) {
            private static final long serialVersionUID = -4958526938290025839L;

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, operator);
            }
        };
    }

    public static TimeLimitedDouble adder(final double initialValue, final Duration ttl) {
        return new TimeLimitedDouble(initialValue, ttl) {
            private static final long serialVersionUID = 8102533948810260223L;

            @Override
            protected double accumulate(final double value) {
                return addAndGet(value);
            }
        };
    }

    public static TimeLimitedDouble peak(final double initialValue, final Duration ttl){
        return new TimeLimitedDouble(initialValue, ttl) {
            private static final long serialVersionUID = -3373087026614450343L;

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, Math::max);
            }
        };
    }

    public static TimeLimitedDouble min(final double initialValue, final Duration ttl){
        return new TimeLimitedDouble(initialValue, ttl) {
            private static final long serialVersionUID = 1548315628295478141L;

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, Math::min);
            }
        };
    }
}
