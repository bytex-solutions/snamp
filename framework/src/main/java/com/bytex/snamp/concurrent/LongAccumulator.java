package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class LongAccumulator extends AbstractAccumulator {
    private static final long serialVersionUID = -8909745382790738723L;
    private static final AtomicLongFieldUpdater<LongAccumulator> CURRENT_VALUE_ACCESSOR =
            AtomicLongFieldUpdater.newUpdater(LongAccumulator.class, "current");
    @SpecialUse
    private volatile long current;
    private final long initialValue;

    /**
     * Initializes a new accumulator of {@literal long} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected LongAccumulator(final long initialValue,
                              final long ttl){
        super(ttl);
        this.current = this.initialValue = initialValue;
    }

    /**
     * Combines the current value of the accumulator with a new value.
     * @param current The current value stored in this accumulator.
     * @param newValue The new value supplied by the caller code.
     * @return A new value.
     */
    protected abstract long combine(final long current, final long newValue);

    private synchronized long reset(final long initialValue) {
        if (isExpired(true))
            CURRENT_VALUE_ACCESSOR.set(this, initialValue);
        return CURRENT_VALUE_ACCESSOR.get(this);
    }

    private long updateImpl(final long value) {
        while (true) {
            final long current = CURRENT_VALUE_ACCESSOR.get(this);
            final long newValue = combine(current, value);
            if (CURRENT_VALUE_ACCESSOR.compareAndSet(this, current, newValue))
                return newValue;
        }
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final long update(final long value){
        return isExpired(false) ? reset(value) : updateImpl(value);
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public final long longValue() {
        return isExpired(false) ? reset(initialValue) : CURRENT_VALUE_ACCESSOR.get(this);
    }

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>int</code>.
     */
    @Override
    public final int intValue() {
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
    public final float floatValue() {
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
    public final double doubleValue() {
        return longValue();
    }

    public static LongAccumulator peak(final long initialValue, final long ttl){
        return new LongAccumulator(initialValue, ttl) {
            private static final long serialVersionUID = 761001354160302714L;

            @Override
            protected long combine(final long current, final long newValue) {
                return Math.max(current, newValue);
            }
        };
    }

    public static LongAccumulator adder(final long initialValue, final long ttl){
        return new LongAccumulator(initialValue, ttl) {
            private static final long serialVersionUID = 2696239067782396417L;

            @Override
            protected long combine(final long current, final long newValue) {
                return current + newValue;
            }
        };
    }
}
