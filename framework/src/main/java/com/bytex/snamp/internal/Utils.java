package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Internal;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.osgi.framework.Constants.OBJECTCLASS;

/**
 * Represents a collection of helpers.
 * <p>
 *     You should not use this class directly in your code.
 * </p>
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Internal
public final class Utils {
    @FunctionalInterface
    private interface SilentInvoker extends Function<Callable<?>, Object> {
        MethodType SIGNATURE = MethodType.methodType(Object.class, Callable.class);//signature after type erasure

        @SpecialUse({SpecialUse.Case.REFLECTION, SpecialUse.Case.JVM})
        <V> V invoke(final Callable<V> callable);

        @Override
        default Object apply(final Callable<?> callable) {
            return invoke(callable);
        }
    }

    private static final SilentInvoker SILENT_INVOKER;
    private static final Cache<Method, Function> GETTERS = CacheBuilder.newBuilder().weakValues().build();
    private static final MethodType GETTER_INVOKED_TYPE = MethodType.methodType(Function.class);
    private static final Cache<Method, BiConsumer> SETTER = CacheBuilder.newBuilder().weakValues().build();
    private static final MethodType SETTER_INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "invoke",
                    MethodType.methodType(SilentInvoker.class),
                    SilentInvoker.SIGNATURE,
                    lookup.findVirtual(Callable.class, "call", MethodType.methodType(Object.class)),
                    SilentInvoker.SIGNATURE);
            SILENT_INVOKER = (SilentInvoker) site.getTarget().invokeExact();
        } catch (final Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Utils(){
        throw new InstantiationError();
    }

    private static String getStackTrace(StackTraceElement[] stackTrace) {
        if (stackTrace.length > 0)
            stackTrace = ArrayUtils.remove(stackTrace, 0);
        return Joiner.on(System.lineSeparator()).join(stackTrace);
    }

    /**
     * Prints the stack trace. Used for debugging purposes.
     */
    public static void printStackTrace(final PrintStream output){
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for(int i = 2; i < stackTrace.length; i++)
            output.println(stackTrace[i]);

    }

    public static BundleContext getBundleContext(final Class<?> classFromBundle){
        final Bundle bnd = FrameworkUtil.getBundle(classFromBundle);
        return bnd != null ? bnd.getBundleContext() : null;
    }

    /**
     * Returns a bundle context associated with bundle which owns the specified object.
     * @param obj An object to be used for context resolving.
     * @return A bundle context associated with bundle which owns the specified object; or {@literal null}
     * if bundle context cannot be resolved.
     */
    public static BundleContext getBundleContextOfObject(final Object obj) {
        return obj != null ? getBundleContext(obj.getClass()) : null;
    }

    private static boolean isInstanceOf(final ServiceReference<?> serviceRef, final String serviceType) {
        final Object names = serviceRef.getProperty(OBJECTCLASS);
        return names instanceof Object[] && ArrayUtils.contains((Object[]) names, serviceType);
    }

    /**
     * Determines whether the specified service reference is a reference of the specified service type.
     * @param serviceRef A reference to check. Cannot be {@literal null}.
     * @param serviceType Expected service type. Cannot be {@literal null}.
     * @return {@literal true}, if the specified reference represents a service of the specified type; otherwise, {@literal false}.
     */
    public static boolean isInstanceOf(final ServiceReference<?> serviceRef, final Class<?> serviceType){
        return isInstanceOf(serviceRef, serviceType.getName());
    }

    /**
     * Opens lexical scope with overridden context class loader.
     * @param newClassLoader Context class loader used in the lexical scope. Cannot be {@literal null}.
     * @return Lexical scope of the overridden context class loader.
     */
    public static SafeCloseable withContextClassLoader(@Nonnull final ClassLoader newClassLoader){
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previous = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(newClassLoader);
        return () -> currentThread.setContextClassLoader(previous);
    }

    /**
     * Used to initialize static fields in interfaces
     * when initialization code may throw exception.
     *
     * @param <T>  the type parameter
     * @param initializer the initializer
     * @return The value returned from initializer.
     * @throws ExceptionInInitializerError the exception in initializer error
     */
    public static <T> T staticInit(final Callable<T> initializer){
        return callAndWrapException(initializer, ExceptionInInitializerError::new);
    }

    public static <T, E extends Throwable> T callAndWrapException(final Callable<? extends T> task, final Function<? super Exception, ? extends E> wrapper) throws E{
        try {
            return task.call();
        } catch (final Exception e) {
            throw wrapper.apply(e);
        }
    }

    public static <I, E extends Throwable> E wrapException(final I input,
                                                             final Exception toWrap,
                                                             final Function<? super I, ? extends E> wrapper) {
        final E newException = wrapper.apply(input);
        newException.initCause(toWrap);
        return newException;
    }


    private static Function createGetter(final MethodHandles.Lookup lookup,
                                         final MethodHandle getter) throws Exception{
        final CallSite site = LambdaMetafactory.metafactory(lookup, "apply",
                GETTER_INVOKED_TYPE,
                MethodType.methodType(Object.class, Object.class),
                getter,
                getter.type());
        try {
            return (Function) site.getTarget().invokeExact();
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable e) {
            throw new Exception(e);
        }
    }

    public static Function reflectGetter(final MethodHandles.Lookup lookup,
                                                            final Method getter) throws ReflectiveOperationException {
        try {
            return GETTERS.get(getter, () -> createGetter(lookup, lookup.unreflect(getter)));
        } catch (final ExecutionException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }

    private static BiConsumer createSetter(final MethodHandles.Lookup lookup,
                                           final MethodHandle setter) throws Exception {
        final CallSite site = LambdaMetafactory.metafactory(lookup,
                "accept",
                SETTER_INVOKED_TYPE,
                MethodType.methodType(void.class, Object.class, Object.class),
                setter,
                setter.type());
        try {
            return (BiConsumer) site.getTarget().invokeExact();
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable e) {
            throw new Exception(e);
        }
    }

    public static BiConsumer reflectSetter(final MethodHandles.Lookup lookup,
                                                               final Method setter) throws ReflectiveOperationException {
        try {
            return SETTER.get(setter, () -> createSetter(lookup, lookup.unreflect(setter)));
        } catch (final ExecutionException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }

    public static <T> void parallelForEach(final Spliterator<T> spliterator,
                                                                       final Consumer<? super T> action,
                                                                       final ExecutorService threadPool) {
        final class ParallelForEachTasks extends LinkedList<Callable<Void>> implements Callable<Object> {
            private static final long serialVersionUID = -2010068532370568252L;

            @Override
            public Object call() throws InterruptedException {
                return threadPool.invokeAll(this);
            }

            private void add(final Spliterator<T> s) {
                add(() -> {
                    s.forEachRemaining(action);
                    return null;
                });
            }
        }

        final ParallelForEachTasks tasks = new ParallelForEachTasks();
        {
            Spliterator<T> subset = spliterator.trySplit();
            for (int i = 0; i < Runtime.getRuntime().availableProcessors() && subset != null; i++, subset = spliterator.trySplit())
                tasks.add(subset);
        }
        tasks.add(spliterator);
        callUnchecked(tasks);
    }

    /**
     * Calls code with checked exception as a code without checked exception.
     * <p>
     *     This method should be used instead of wrapping some code into try-catch block with ignored exception.
     *     Don't use this method to hide checked exception that can be actually happened at runtime in some conditions.
     * @param callable Portion of code to execute.
     * @param <V> Type of result.
     * @return An object returned by portion of code.
     */
    public static <V> V callUnchecked(final Callable<V> callable) {
        return SILENT_INVOKER.invoke(callable);
    }

    /**
     * Closes many resources in guaranteed manner.
     * @param resources A set of resources to close.
     * @throws Exception One or more resource throw exception when closing.
     */
    public static void closeAll(final AutoCloseable... resources) throws Exception {
        Exception e = null;
        for (final AutoCloseable closeable : resources)
            if (closeable != null)
                try {
                    closeable.close();
                } catch (final Exception inner) {
                    if (e == null)
                        e = inner;
                    else
                        e.addSuppressed(inner);
                }
        if (e != null)
            throw e;
    }
}
