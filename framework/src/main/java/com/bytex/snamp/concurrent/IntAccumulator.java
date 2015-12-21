package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class IntAccumulator extends AbstractAccumulator {
    private static final AtomicIntegerFieldUpdater<IntAccumulator> CURRENT_VALUE_ACCESSOR =
            AtomicIntegerFieldUpdater.newUpdater(IntAccumulator.class, "current");
    private static final long serialVersionUID = 5460812167708036224L;
    @SpecialUse
    private volatile int current;
    private final int initialValue;

    /**
     * Initializes a new accumulator of {@literal int} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected IntAccumulator(final int initialValue,
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
    protected abstract int combine(final int current, final int newValue);

    @Override
    public final synchronized void reset(){
        super.reset();
        CURRENT_VALUE_ACCESSOR.set(this, initialValue);
    }

    private int updateImpl(final int value) {
        int newValue;
        do {
            final int current = CURRENT_VALUE_ACCESSOR.get(this);
            newValue = combine(current, value);
        }
        while (!CURRENT_VALUE_ACCESSOR.compareAndSet(this, current, newValue));
        return newValue;
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final int update(final int value){
        if(isExpired())
            reset();
        return updateImpl(value);
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public final long longValue() {
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
    public final int intValue() {
        if(isExpired())
            reset();
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
    public final float floatValue() {
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
    public final double doubleValue() {
        return intValue();
    }

    public static IntAccumulator peak(final int initialValue, final long ttl){
        return new IntAccumulator(initialValue, ttl) {
            private static final long serialVersionUID = 7620900607067251500L;

            @Override
            protected int combine(final int current, final int newValue) {
                return Math.max(current, newValue);
            }
        };
    }

    public static IntAccumulator adder(final int initialValue, final long ttl){
        return new IntAccumulator(initialValue, ttl) {
            private static final long serialVersionUID = -8158828259518423267L;

            @Override
            protected int combine(final int current, final int newValue) {
                return current + newValue;
            }
        };
    }
}