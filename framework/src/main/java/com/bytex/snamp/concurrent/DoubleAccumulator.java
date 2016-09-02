package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.math.StatefulDoubleUnaryFunction;
import com.bytex.snamp.math.UnaryFunctions;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.*;

/**
 * Represents time-based accumulator for {@code double} numbers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public abstract class DoubleAccumulator extends AbstractAccumulator implements DoubleSupplier, DoubleConsumer {
    private static final long serialVersionUID = 5535293508302818992L;
    private static final AtomicLongFieldUpdater<DoubleAccumulator> CURRENT_VALUE_ACCESSOR = AtomicLongFieldUpdater.newUpdater(DoubleAccumulator.class, "current");

    private final double initialValue;
    @SpecialUse
    private volatile long current;

    /**
     * Initializes a new accumulator of {@literal double} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected DoubleAccumulator(final double initialValue,
                              final LongSupplier ttl) {
        super(ttl);
        this.initialValue = initialValue;
        this.current = Double.doubleToLongBits(initialValue);
    }

    private void setCurrentValue(final double value){
        CURRENT_VALUE_ACCESSOR.set(this, Double.doubleToLongBits(value));
    }

    private double getCurrentValue(){
        return Double.longBitsToDouble(CURRENT_VALUE_ACCESSOR.get(this));
    }

    @Override
    public synchronized void reset() {
        super.reset();
        setCurrentValue(initialValue);
    }

    private synchronized void resetIfExpired() {
        if (isExpired())
            reset();
    }

    /**
     * Atomically combines a new value with existing using given function.
     * @param operator A side-effect-free function used to combine two values. Cannot be {@literal null}.
     * @param newValue The value passed from {@link #update(double)} method.
     * @return The updated value.
     * @since 1.2
     */
    protected final long accumulateAndGet(final DoubleBinaryOperator operator, final double newValue) {
        return CURRENT_VALUE_ACCESSOR.accumulateAndGet(this,
                Double.doubleToLongBits(newValue),
                (x, y) -> Double.doubleToLongBits(operator.applyAsDouble(Double.longBitsToDouble(x), Double.longBitsToDouble(y))));
    }

    /**
     * Combines the value in this accumulator with the new one.
     * @param value A value comes for {@link #update(double)} method.
     * @return A new combined value.
     */
    protected abstract double accumulate(final double value);

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final double update(final double value) {
        if (isExpired())
            resetIfExpired();   //double check required
        return accumulate(value);
    }

    /**
     * Returns the value of the specified number as an {@code int},
     * which may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code int}.
     */
    @Override
    public final int intValue() {
        return Math.round(floatValue());
    }

    /**
     * Returns the value of the specified number as a {@code long},
     * which may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code long}.
     */
    @Override
    public final long longValue() {
        return Math.round(doubleValue());
    }

    /**
     * Returns the value of the specified number as a {@code float},
     * which may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code float}.
     */
    @Override
    public final float floatValue() {
        return (float) doubleValue();
    }

    /**
     * Returns the value of the specified number as a {@code double},
     * which may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code double}.
     */
    @Override
    public final double doubleValue() {
        if (isExpired())
            resetIfExpired();   //double check required
        return getCurrentValue();
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public final void accept(final double value) {
        update(value);
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public final double getAsDouble() {
        return doubleValue();
    }

    public static DoubleAccumulator create(final double initialValue, final long ttl, final DoubleBinaryOperator accumulator) {
        return new DoubleAccumulator(initialValue, () -> ttl) {
            private static final long serialVersionUID = 3896687805468634111L;

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(accumulator, value);
            }
        };
    }

    public static DoubleAccumulator average(final double initialValue, final long ttl) {
        return new DoubleAccumulator(initialValue, () -> ttl) {
            private static final long serialVersionUID = 7215017494947382326L;
            private final StatefulDoubleUnaryFunction average = UnaryFunctions.average();

            @Override
            protected synchronized double accumulate(final double value) {
                final double avg = average.applyAsDouble(value);
                super.setCurrentValue(avg);
                return avg;
            }

            @Override
            public synchronized void reset() {
                super.reset();
                average.reset();
            }
        };
    }
}
