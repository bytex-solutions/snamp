package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedBox;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicReference;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents distributed Box service.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class HazelcastBox extends HazelcastSharedObject<IAtomicReference<Serializable>> implements SharedBox {
    HazelcastBox(final HazelcastInstance hazelcast, final String name){
        super(hazelcast, name, HazelcastInstance::getAtomicReference);
    }

    @Override
    public Serializable get() {
        return getDistributedObject().get();
    }

    @Override
    public void reset() {
        getDistributedObject().clear();
    }

    @Override
    public void set(final Serializable value) {
        getDistributedObject().set(value);
    }

    @Override
    public Serializable setIfAbsent(final Supplier<? extends Serializable> valueProvider) {
        Serializable current;
        do {
            current = get();
            if (current == null)
                current = valueProvider.get();
            else
                break;
        } while (!getDistributedObject().compareAndSet(null, current));
        return current;
    }

    @Override
    public Serializable accumulateAndGet(final Serializable right, final BinaryOperator<Serializable> operator) {
        Serializable prev, next;
        do{
            next = operator.apply(prev = getDistributedObject().get(), right);
        } while (!getDistributedObject().compareAndSet(prev, next));
        return next;
    }

    @Override
    public Serializable updateAndGet(final UnaryOperator<Serializable> operator) {
        Serializable prev, next;
        do{
            next = operator.apply(prev = getDistributedObject().get());
        } while (!getDistributedObject().compareAndSet(prev, next));
        return next;
    }

    @Override
    public Serializable getAndSet(final Serializable newValue) {
        return getDistributedObject().getAndSet(newValue);
    }

    @Override
    public Serializable getOrDefault(final Supplier<? extends Serializable> defaultProvider) {
        final Object current = get();
        return current == null ? defaultProvider.get() : get();
    }

    @Override
    public <R> Optional<R> map(final Function<? super Serializable, ? extends R> mapper) {
        return Optional.ofNullable(get()).map(mapper);
    }

    /**
     * Determines whether this container has stored value.
     *
     * @return {@literal true}, if this container has stored value.
     */
    @Override
    public boolean hasValue() {
        return !getDistributedObject().isNull();
    }

    @Override
    public void accept(final Serializable value) {
        getDistributedObject().set(value);
    }
}
