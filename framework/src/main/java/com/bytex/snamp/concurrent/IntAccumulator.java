package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.IntBinaryOperator;

/**
 * Represents time-based accumulator for {@code int} numbers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class IntAccumulator extends AbstractAccumulator {
    private static final AtomicIntegerFieldUpdater<IntAccumulator> CURRENT_VALUE_ACCESSOR =
            AtomicIntegerFieldUpdater.newUpdater(IntAccumulator.class, "current");
    private static final long serialVersionUID = 5460812167708036224L;
    @SpecialUse
    private volatile int current;
    private final int initialValue;
    private final IntBinaryOperator accumulator;

    /**
     * Initializes a new accumulator of {@literal int} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     * @param accumulator A side-effect-free function used to accumulate two {@code int}s. Cannot be {@literal null}.
     */
    public IntAccumulator(final int initialValue,
                             final long ttl,
                             final IntBinaryOperator accumulator){
        super(ttl);
        this.current = this.initialValue = initialValue;
        this.accumulator = Objects.requireNonNull(accumulator);
    }

    @Override
    public synchronized void reset(){
        super.reset();
        CURRENT_VALUE_ACCESSOR.set(this, initialValue);
    }

    private synchronized void resetIfExpired(){
        if(isExpired())
            reset();
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public int update(final int value){
        if(isExpired())
            resetIfExpired();   //double check required
        return CURRENT_VALUE_ACCESSOR.accumulateAndGet(this, value, accumulator);
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public long longValue() {
        return intValue();
    }

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>int</code>.
     */
    @Override
    public int intValue() {
        if(isExpired())
            resetIfExpired();   //double check required
        return CURRENT_VALUE_ACCESSOR.get(this);
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>float</code>.
     */
    @Override
    public float floatValue() {
        return intValue();
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>double</code>.
     */
    @Override
    public double doubleValue() {
        return intValue();
    }

    public static IntAccumulator peak(final int initialValue, final long ttl){
        return new IntAccumulator(initialValue, ttl, Math::max);
    }

    public static IntAccumulator adder(final int initialValue, final long ttl) {
        return new IntAccumulator(initialValue, ttl, (current, value) -> current + value);
    }
}
