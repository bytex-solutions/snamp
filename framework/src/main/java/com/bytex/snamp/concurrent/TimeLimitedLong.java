package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.ThreadSafe;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/**
 * Represents time-constrained accumulator for {@code long} numbers.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public abstract class TimeLimitedLong extends TimeLimited implements LongSupplier, LongConsumer {
    @SpecialUse
    private final AtomicLong current;
    private final long initialValue;

    /**
     * Initializes a new accumulator of {@literal long} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected TimeLimitedLong(final long initialValue,
                              final LongSupplier ttl){
        super(ttl);
        current = new AtomicLong(this.initialValue = initialValue);
    }

    private void setInitialValue(){
        current.set(initialValue);
    }

    private void setInitialValue(final LongConsumer callback){
        callback.accept(current.getAndSet(initialValue));
    }

    @Override
    public final void reset(){
        super.reset();
        setInitialValue();
    }

    private void resetIfExpired() {
        acceptIfExpired(this, TimeLimitedLong::setInitialValue);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta The value to add.
     * @return The updated value.
     * @since 1.2
     */
    protected final long addAndGet(final long delta){
        return current.addAndGet(delta);
    }

    /**
     * Atomically combines a new value with existing using given function.
     * @param operator A side-effect-free function used to combine two values. Cannot be {@literal null}.
     * @param newValue The value passed from {@link #update(long)} method.
     * @return The updated value.
     * @since 1.2
     */
    protected final long accumulateAndGet(final LongBinaryOperator operator, final long newValue){
        return current.accumulateAndGet(newValue, operator);
    }

    /**
     * Combines the value in this accumulator with the new one.
     * @param value A value comes for {@link #update(long)} method.
     * @return A new combined value.
     * @see #accumulateAndGet(LongBinaryOperator, long)
     * @see #addAndGet(long)
     */
    protected abstract long accumulate(final long value);

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    public final long update(final long value) {
        resetIfExpired();
        return accumulate(value);
    }

    public final long update(final long value, final LongConsumer callback){
        acceptIfExpired(this, callback, TimeLimitedLong::setInitialValue);
        return accumulate(value);
    }

    /**
     * Updates this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     */
    @Override
    public final void accept(final long value) {
        update(value);
    }

    public long updateByOne(){
        return update(1L);
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public final long getAsLong() {
        resetIfExpired();
        return current.get();
    }

    @Override
    public final String toString() {
        return Long.toString(current.get());
    }

    public static TimeLimitedLong create(final long initialValue, final long ttl, final LongBinaryOperator accumulator){
        return new TimeLimitedLong(initialValue, () -> ttl) {

            @Override
            protected long accumulate(final long value) {
                return accumulateAndGet(accumulator, value);
            }
        };
    }

    public static TimeLimitedLong peak(final long initialValue, final long ttl){
        return create(initialValue, ttl, Math::max);
    }

    public static TimeLimitedLong min(final long initialValue, final long ttl){
        return create(initialValue, ttl, Math::min);
    }

    public static TimeLimitedLong adder(final long initialValue, final long ttl) {
        final class Adder extends TimeLimitedLong {

            private Adder(final long initialValue, final long ttl){
                super(initialValue, () -> ttl);
            }

            @Override
            protected long accumulate(final long delta) {
                return addAndGet(delta);
            }
        }

        return new Adder(initialValue, ttl);
    }
}
