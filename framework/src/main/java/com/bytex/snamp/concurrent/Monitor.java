package com.bytex.snamp.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.ExceptionalCallable;

import java.util.concurrent.Callable;

/**
 * Represents functional version of the Java Monitor.
 * <p>
 *     You can use an instance of this object as a monitor for exclusive
 *     invocation of some portion of code. Also, it is possible to override
 *     this class and add the additional synchronized invocation helpers.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class Monitor {

    /**
     * Invokes the specified portion of code in synchronized manner.
     * @param block The block of code to be executed in synchronized manner.
     * @param <V> The result of code block invocation.
     * @param <E> An exception that can be produced by the specified block of code.
     * @return The result of the code block invocation.
     * @throws E An error occurred in the specified block of code.
     */
    public final synchronized  <V, E extends Exception> V synchronizedInvoke(final ExceptionalCallable<V, E> block) throws E {
        return block.call();
    }

    /**
     * Invokes the specified portion of code in synchronized manner.
     * @param block The block of code to be executed in synchronized manner.
     * @param <V> The result of code block invocation.
     * @return The result of the code block invocation.
     * @throws java.lang.Exception An error occurred in the specified block of code.
     */
    public final synchronized <V> V synchronizedInvoke(final Callable<V> block) throws Exception {
        return block.call();
    }

    /**
     * Invokes the specified portion of code in synchronized manner.
     * @param block The block of code to be executed in synchronized manner.
     * @param input The input value for code block.
     * @param <I> Type of the input value for code block.
     * @param <O> The result of code block invocation.
     * @return The result of the code block invocation.
     */
    public final synchronized  <I, O> O synchronizedInvoke(final Function<I, O> block, final I input){
        return block.apply(input);
    }

    /**
     * Invokes the specified portion of code in synchronized manner.
     * @param block The block of code to be executed in synchronized manner.
     * @param <O> The result of code block invocation.
     * @return The result of the code block invocation.
     */
    public final synchronized  <O> O synchronizedInvoke(final Supplier<O> block){
        return block.get();
    }

    /**
     * Invokes the specified portion of code in synchronized manner.
     * @param block The block of code to be executed in synchronized manner.
     * @param input The input value for code block.
     * @param <I> Type of the input value for code block.
     * @param <E> An exception that can be produced by the specified block of code.
     * @throws E An error occurred in the specified block of code.
     */
    public final synchronized <I, E extends Throwable> void synchronizedInvoke(final Consumer<I, E> block, final I input) throws E {
        block.accept(input);
    }
}
