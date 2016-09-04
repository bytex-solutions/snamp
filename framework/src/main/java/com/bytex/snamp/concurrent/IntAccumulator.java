package com.bytex.snamp.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
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
    private static final long serialVersionUID = 5460812167708036224L;
    private final AtomicInteger current;
    private final int initialValue;

    /**
     * Initializes a new accumulator of {@literal int} values.
     * @param initialValue The initial value of this accumulator.
     * @param ttl Time-to-live of the value in this accumulator, in millis.
     */
    protected IntAccumulator(final int initialValue,
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
        acceptIfExpired(this, IntAccumulator::setInitialValue);
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
        resetIfExpired();
        return current.get();
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
