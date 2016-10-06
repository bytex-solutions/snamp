package com.bytex.snamp.concurrent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents time-based accumulator for Java objects.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see TimeLimitedLong
 * @see TimeLimitedInt
 */
public final class TimeLimitedObject<V> extends TimeLimited implements Consumer<V>, Supplier<V> {
    private final AtomicReference<V> storage;
    private final V initialValue;
    private final BinaryOperator<V> operator;

    public TimeLimitedObject(final V initialValue, final Duration ttl, final BinaryOperator<V> operator) {
        super(ttl);
        storage = new AtomicReference<>(this.initialValue = initialValue);
        this.operator = Objects.requireNonNull(operator);
    }

    private void setInitialValue(){
        storage.set(initialValue);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        super.reset();
        setInitialValue();
    }

    private void resetIfExpired(){
        acceptIfExpired(this, TimeLimitedObject::setInitialValue);
    }

    /**
     * Updates a value inside of this container.
     * @param value A new value used to update this container.
     * @return Accumulated value.
     */
    public V update(final V value){
        resetIfExpired();
        return storage.accumulateAndGet(value, operator);
    }

    /**
     * Updates a value inside of this container.
     * @param value A new value used to update this container.
     */
    @Override
    public void accept(final V value) {
        update(value);
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public V get() {
        resetIfExpired();
        return storage.get();
    }
    @Override
    public String toString() {
        return Objects.toString(storage.get());
    }
}
