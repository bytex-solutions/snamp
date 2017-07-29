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
 * @version 2.1
 * @since 2.0
 * @see TimeLimitedLong
 * @see TimeLimitedInt
 */
public final class TimeLimitedObject<V> extends Timeout implements Consumer<V>, Supplier<V>, Cloneable {
    private static final long serialVersionUID = -4520377005117627998L;
    private final AtomicReference<V> storage;
    private final V initialValue;
    private final BinaryOperator<V> operator;

    public TimeLimitedObject(final V initialValue, final Duration ttl, final BinaryOperator<V> operator) {
        super(ttl);
        storage = new AtomicReference<>(this.initialValue = initialValue);
        this.operator = Objects.requireNonNull(operator);
    }

    private TimeLimitedObject(final TimeLimitedObject<V> source) {
        super(source);
        storage = new AtomicReference<>(source.storage.get());
        initialValue = source.initialValue;
        operator = source.operator;
    }

    public TimeLimitedObject<V> clone(){
        return new TimeLimitedObject<>(this);
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

    private void resetIfNecessary(){
        acceptIfExpired(this, TimeLimitedObject::setInitialValue);
    }

    /**
     * Updates a value inside of this container.
     * @param value A new value used to update this container.
     * @return Accumulated value.
     */
    public V update(final V value){
        resetIfNecessary();
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
        resetIfNecessary();
        return storage.get();
    }
    @Override
    public String toString() {
        return Objects.toString(storage.get());
    }
}
