package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of soft- or weak- referenced singletons.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
abstract class AbstractLazyReference<V> extends AtomicReference<Reference<V>> implements LazyReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;

    abstract Reference<V> makeRef(final V value);

    private synchronized <I> V initAndGet(final I input, final Function<? super I, ? extends V> initializer) {
        final Reference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            accept(result = initializer.apply(input));
        return result;
    }

    @Override
    public final  <I> V lazyGet(final I input, final Function<? super I, ? extends V> initializer) {
        final Reference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(input, initializer) : result;
    }

    private synchronized <I1, I2> V initAndGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer) {
        final Reference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            accept(result = initializer.apply(input1, input2));
        return result;
    }

    @Override
    public final <I1, I2> V lazyGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer) {
        final Reference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(input1, input2, initializer) : result;
    }

    private synchronized <E extends Throwable> V initAndGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        final Reference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null) {
            final Box<V> valueHolder = BoxFactory.create(null);
            initializer.accept(valueHolder);
            accept(result = valueHolder.get());
            valueHolder.reset();    //help GC
        }
        return result;
    }

    @Override
    public final <E extends Throwable> V lazyGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        final Reference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(initializer) : result;
    }

    private synchronized V initAndGet(final Supplier<? extends V> initializer) {
        final Reference<V> softRef = get();
        V result;
        if (softRef == null || (result = softRef.get()) == null)
            accept(result = initializer.get());
        return result;
    }

    @Override
    public final V lazyGet(final Supplier<? extends V> initializer) {
        final Reference<V> softRef = get();
        final V result;
        return softRef == null || (result = softRef.get()) == null ? initAndGet(initializer) : result;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public final void accept(final V newValue) {
        set(makeRef(newValue));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public final void reset() {
        final Reference<?> reference = getAndSet(null);
        if(reference != null)
            reference.clear();  //help GC
    }
}
