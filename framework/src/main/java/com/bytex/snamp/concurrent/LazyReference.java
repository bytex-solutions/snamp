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
 * @version 2.1
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

    /**
     * Gets value stored in this container.
     * @return A value stored in this container.
     */
    V getValue();

    boolean reset(final Consumer<? super V> consumer);

    /**
     * Creates a new container with lazy initialization which stores strong reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> strong(){
        return new LazyStrongReference<>();
    }

    /**
     * Creates a new container with lazy initialization which stores soft reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> soft(){
        return new LazySoftReference<>();
    }

    /**
     * Creates a new container with lazy initialization which stores weak reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> weak(){
        return new LazyWeakReference<>();
    }
}
