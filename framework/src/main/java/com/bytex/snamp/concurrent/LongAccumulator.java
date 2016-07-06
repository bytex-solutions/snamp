package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.LongBinaryOperator;

/**
 * Represents time-based accumulator for {@code long} numbers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class LongAccumulator extends AbstractAccumulator {
    private static final long serialVersionUID = -8909745382790738723L;
    private static final AtomicLongFieldUpdater<LongAccumulator> CURRENT_VALUE_ACCESSOR =
            AtomicLongFieldUpdater.newUpdater(LongAccumulator.class, "current");
    @SpecialUse
    private volatile long current;
    private final long initialValue;
    private final LongBinaryOperator accumulator;

    /**
     * Initializes a new accumulator of {@literal long} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     * @param accumulator A side-effect-free function used to accumulate two {@code long}s. Cannot be {@literal null}.
     */
    public LongAccumulator(final long initialValue,
                              final long ttl,
                              final LongBinaryOperator accumulator){
        super(ttl);
        this.current = this.initialValue = initialValue;
        this.accumulator = Objects.requireNonNull(accumulator);
    }

    @Override
    public synchronized void reset(){
        super.reset();
        CURRENT_VALUE_ACCESSOR.set(this, initialValue);
    }

    private synchronized void resetIfExpired() {
        if (isExpired())
            reset();
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public long update(final long value) {
        if (isExpired())
            resetIfExpired();   //double check required
        return CURRENT_VALUE_ACCESSOR.accumulateAndGet(this, value, accumulator);
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public long longValue() {
        if(isExpired())
            resetIfExpired();   //double check required
        return CURRENT_VALUE_ACCESSOR.get(this);
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
        return (int)longValue();
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
        return longValue();
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
        return longValue();
    }

    public static LongAccumulator peak(final long initialValue, final long ttl){
        return new LongAccumulator(initialValue, ttl, Math::max);
    }

    public static LongAccumulator adder(final long initialValue, final long ttl) {
        return new LongAccumulator(initialValue, ttl, (current, value) -> current + value);
    }
}
