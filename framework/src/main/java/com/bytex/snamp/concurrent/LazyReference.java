package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Stateful;

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
public interface LazyReference<V> extends Consumer<V>, Stateful {
    default V lazyGet(final Supplier<? extends V> initializer){
        V result = getValue();
        if(result == null)
            synchronized (this){
                result = getValue();
                if(result == null)
                    accept(result = initializer.get());
            }
        return result;
    }

    default <I> V lazyGet(final I input, final Function<? super I, ? extends V> initializer){
        V result = getValue();
        if(result == null)
            synchronized (this){
                result = getValue();
                if(result == null)
                    accept(result = initializer.apply(input));
            }
        return result;
    }

    default <I1, I2> V lazyGet(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer){
        V result = getValue();
        if(result == null)
            synchronized (this){
                result = getValue();
                if(result == null)
                    accept(result = initializer.apply(input1, input2));
            }
        return result;
    }

    default <E extends Throwable> V lazyGet(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        V result = getValue();
        if (result == null)
            synchronized (this) {
                result = getValue();
                if (result == null) {
                    initializer.accept(this);
                    result = getValue();
                }
            }
        return result;
    }

    V getValue();

    static <V> LazyReference<V> strong(){
        return new LazyStrongReference<>();
    }

    static <V> LazyReference<V> soft(){
        return new LazySoftReference<>();
    }

    static <V> LazyReference<V> weak(){
        return new LazyWeakReference<>();
    }
}
