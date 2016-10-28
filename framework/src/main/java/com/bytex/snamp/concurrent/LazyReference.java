package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a reference to object with lazy initialization.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface LazyReference<V> extends Consumer<V> {
    V lazyGet(final Supplier<? extends V> initializer);
    <I> V lazyGet(final I input, final Function<? super I, ? extends V> initializer);
    <I1, I2> V lazyGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer);
    <E extends Throwable> V lazyGet(final Acceptor<? super Consumer<V>, E> initializer) throws E;
}
