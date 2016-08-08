package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Represents time-based accumulator for {@code int} numbers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class IntAccumulator extends AbstractAccumulator implements IntSupplier, IntConsumer {
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
                             final LongSupplier ttl){
        super(ttl);
        this.current = this.initialValue = initialValue;
    }

    @Override
    public final synchronized void reset(){
        super.reset();
        CURRENT_VALUE_ACCESSOR.set(this, initialValue);
    }

    private synchronized void resetIfExpired(){
        if(isExpired())
            reset();
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta The value to add.
     * @return The updated value.
     * @since 1.2
     */
    protected final int addAndGet(final int delta){
        return CURRENT_VALUE_ACCESSOR.addAndGet(this, delta);
    }

    /**
     * Atomically combines a new value with existing using given function.
     * @param operator A side-effect-free function used to combine two values. Cannot be {@literal null}.
     * @param newValue The value passed from {@link #update(int)} method.
     * @return The updated value.
     * @since 1.2
     */
    protected final int accumulateAndGet(final IntBinaryOperator operator, final int newValue){
        return CURRENT_VALUE_ACCESSOR.accumulateAndGet(this, newValue, operator);
    }

    protected abstract int accumulate(final int value);

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final int update(final int value){
        if(isExpired())
            resetIfExpired();   //double check required
        return accumulate(value);
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     */
    @Override
    public final void accept(final int value) {
        update(value);
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
    public final int getAsInt() {
        return intValue();
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

    public static IntAccumulator create(final int initialValue, final long ttl, final IntBinaryOperator accumulator){
        return new IntAccumulator(initialValue, () -> ttl) {
            private static final long serialVersionUID = -3940528661924869135L;

            @Override
            protected int accumulate(final int value) {
                return accumulateAndGet(accumulator, value);
            }
        };
    }

    public static IntAccumulator peak(final int initialValue, final long ttl){
        return create(initialValue, ttl, Math::max);
    }

    public static IntAccumulator adder(final int initialValue, final long ttl) {
        final class Adder extends IntAccumulator{
            private static final long serialVersionUID = 8155999174928846425L;

            private Adder(final int initialValue, final long ttl){
                super(initialValue, () -> ttl);
            }

            @Override
            protected int accumulate(final int delta) {
                return addAndGet(delta);
            }
        }

        return new Adder(initialValue, ttl);
    }
}
