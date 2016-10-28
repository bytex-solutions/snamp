package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of Initialization-On-Demand pattern
 * applied to fields.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see LazySoftReference
 */
public final class LazyStrongReference<V> extends AtomicReference<V> implements LazyReference<V> {
    private static final long serialVersionUID = 813414681121113370L;

    private synchronized <I> V initAndGet(final I input, final Function<? super I, ? extends V> initializer){
        V result = get();
        if(result == null)
            set(result = initializer.apply(input));
        return result;
    }

    @Override
    public <I> V lazyGet(final I input, final Function<? super I, ? extends V> initializer) {
        final V result = get();
        return result == null ? initAndGet(input, initializer) : result;
    }

    private synchronized <I1, I2> V initAndGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer){
        V result = get();
        if(result == null)
            set(result = initializer.apply(input1, input2));
        return result;
    }

    @Override
    public <I1, I2> V lazyGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer){
        final V result = get();
        return result == null ? initAndGet(input1, input2, initializer) : result;
    }

    private synchronized <E extends Throwable> V initAndGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        if (get() == null)
            initializer.accept(this);
        return get();
    }

    @Override
    public <E extends Throwable> V lazyGet(final Acceptor<? super Consumer<V>, E> initializer) throws E{
        final V result = get();
        return result == null ? initAndGet(initializer) : result;
    }

    private synchronized V initAndGet(final Supplier<? extends V> initializer){
        V result = get();
        if(result == null)
            set(result = initializer.get());
        return result;
    }

    @Override
    public V lazyGet(final Supplier<? extends V> initializer) {
        final V result = get();
        return result == null ? initAndGet(initializer) : result;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public void accept(final V newValue) {
        set(newValue);
    }
}
