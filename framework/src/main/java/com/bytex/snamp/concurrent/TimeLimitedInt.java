package com.bytex.snamp.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Represents time-constrained accumulator for {@code int} numbers.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class TimeLimitedInt extends TimeLimited implements IntSupplier, IntConsumer {
    private final AtomicInteger current;
    private final int initialValue;

    /**
     * Initializes a new accumulator of {@literal int} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected TimeLimitedInt(final int initialValue,
                             final LongSupplier ttl){
        super(ttl);
        current = new AtomicInteger(this.initialValue = initialValue);
    }

    @Override
    public final void reset(){
        super.reset();
        setInitialValue();
    }

    private void setInitialValue(){
        current.set(initialValue);
    }

    private void resetIfExpired(){
        acceptIfExpired(this, TimeLimitedInt::setInitialValue);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta The value to add.
     * @return The updated value.
     * @since 1.2
     */
    protected final int addAndGet(final int delta){
        return current.addAndGet(delta);
    }

    /**
     * Atomically combines a new value with existing using given function.
     * @param operator A side-effect-free function used to combine two values. Cannot be {@literal null}.
     * @param newValue The value passed from {@link #update(int)} method.
     * @return The updated value.
     * @since 1.2
     */
    protected final int accumulateAndGet(final IntBinaryOperator operator, final int newValue){
        return current.accumulateAndGet(newValue, operator);
    }

    protected abstract int accumulate(final int value);

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final int update(final int value){
        resetIfExpired();
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
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>int</code>.
     */
    @Override
    public final int getAsInt() {
        resetIfExpired();
        return current.get();
    }

    @Override
    public final String toString() {
        return Integer.toString(current.get());
    }

    public static TimeLimitedInt create(final int initialValue, final long ttl, final IntBinaryOperator accumulator){
        return new TimeLimitedInt(initialValue, () -> ttl) {

            @Override
            protected int accumulate(final int value) {
                return accumulateAndGet(accumulator, value);
            }
        };
    }

    public static TimeLimitedInt peak(final int initialValue, final long ttl){
        return create(initialValue, ttl, Math::max);
    }

    public static TimeLimitedInt adder(final int initialValue, final long ttl) {
        final class Adder extends TimeLimitedInt {

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
