package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of soft-referenced singletons.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LazySoftReference<V> extends AtomicReference<SoftReference<V>> implements LazyReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;

    private synchronized <I> V initAndGet(final I input, final Function<? super I, ? extends V> initializer) {
        final SoftReference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            set(new SoftReference<>(result = initializer.apply(input)));
        return result;
    }

    @Override
    public <I> V lazyGet(final I input, final Function<? super I, ? extends V> initializer) {
        final SoftReference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(input, initializer) : result;
    }

    private synchronized <I1, I2> V initAndGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer) {
        final SoftReference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            set(new SoftReference<>(result = initializer.apply(input1, input2)));
        return result;
    }

    @Override
    public <I1, I2> V lazyGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer) {
        final SoftReference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(input1, input2, initializer) : result;
    }

    private synchronized <E extends Throwable> V initAndGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        final SoftReference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null) {
            final Box<V> valueHolder = BoxFactory.create(null);
            initializer.accept(valueHolder);
            accept(valueHolder.get());
            return valueHolder.get();
        }
        else
            return result;
    }

    @Override
    public <E extends Throwable> V lazyGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        final SoftReference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(initializer) : result;
    }

    private synchronized V initAndGet(final Supplier<? extends V> initializer) {
        final SoftReference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            set(new SoftReference<>(result = initializer.get()));
        return result;
    }

    @Override
    public V lazyGet(final Supplier<? extends V> initializer) {
        final SoftReference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(initializer) : result;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public void accept(final V newValue) {
        set(new SoftReference<>(newValue));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        final SoftReference<?> reference = getAndSet(null);
        if(reference != null)
            reference.clear();  //help GC
    }
}
