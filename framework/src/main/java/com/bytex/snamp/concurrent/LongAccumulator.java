package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.ThreadSafe;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/**
 * Represents time-based accumulator for {@code long} numbers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public abstract class LongAccumulator extends AbstractAccumulator implements LongSupplier, LongConsumer {
    private static final long serialVersionUID = -8909745382790738723L;
    @SpecialUse
    private final AtomicLong current;
    private final long initialValue;

    /**
     * Initializes a new accumulator of {@literal long} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected LongAccumulator(final long initialValue,
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
        acceptIfExpired(this, LongAccumulator::setInitialValue);
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
        acceptIfExpired(this, callback, LongAccumulator::setInitialValue);
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

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public final long longValue() {
        resetIfExpired();
        return current.get();
    }

    /**
     * Gets value of this accumulator.
     * @return Value of this accumulator
     */
    @Override
    public final long getAsLong() {
        return longValue();
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

    public static LongAccumulator create(final long initialValue, final long ttl, final LongBinaryOperator accumulator){
        return new LongAccumulator(initialValue, () -> ttl) {
            private static final long serialVersionUID = 3896687805468634111L;

            @Override
            protected long accumulate(final long value) {
                return accumulateAndGet(accumulator, value);
            }
        };
    }

    public static LongAccumulator peak(final long initialValue, final long ttl){
        return create(initialValue, ttl, Math::max);
    }

    public static LongAccumulator min(final long initialValue, final long ttl){
        return create(initialValue, ttl, Math::min);
    }

    public static LongAccumulator adder(final long initialValue, final long ttl) {
        final class Adder extends LongAccumulator{
            private static final long serialVersionUID = 8583924012634668053L;

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
