package com.bytex.snamp.concurrent;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Represents time-constrained accumulator for {@code int} numbers.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class TimeLimitedInt extends Timeout implements IntSupplier, IntConsumer {
    private static final long serialVersionUID = -3529410942029219094L;
    private final AtomicInteger current;
    private final int initialValue;

    /**
     * Initializes a new accumulator of {@literal int} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator. Cannot be {@literal null}.
     */
    protected TimeLimitedInt(final int initialValue,
                             final Duration ttl){
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

    private void resetIfNecessary(){
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
     * @param newValue The value passed from {@link #update(int)} method.
     * @param operator A side-effect-free function used to combine two values. Cannot be {@literal null}.
     * @return The updated value.
     * @since 1.2
     */
    protected final int accumulateAndGet(final int newValue, final IntBinaryOperator operator){
        return current.accumulateAndGet(newValue, operator);
    }

    protected abstract int accumulate(final int value);

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final int update(final int value){
        resetIfNecessary();
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
        resetIfNecessary();
        return current.get();
    }

    @Override
    public final String toString() {
        return Integer.toString(current.get());
    }

    public static TimeLimitedInt create(final int initialValue, final Duration ttl, final IntBinaryOperator accumulator){
        return new TimeLimitedInt(initialValue, ttl) {
            private static final long serialVersionUID = 6051079718988961185L;

            @Override
            protected int accumulate(final int value) {
                return accumulateAndGet(value, accumulator);
            }
        };
    }

    public static TimeLimitedInt peak(final int initialValue, final Duration ttl){
        return new TimeLimitedInt(initialValue, ttl) {
            private static final long serialVersionUID = -8486723696320618514L;

            @Override
            protected int accumulate(final int value) {
                return accumulateAndGet(value, Math::max);
            }
        };
    }

    public static TimeLimitedInt adder(final int initialValue, final Duration ttl) {
        return new TimeLimitedInt(initialValue, ttl) {
            private static final long serialVersionUID = -8664657963798986570L;

            @Override
            protected int accumulate(final int delta) {
                return addAndGet(delta);
            }
        };
    }
}
