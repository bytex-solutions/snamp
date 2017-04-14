package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Internal;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.osgi.framework.Constants.OBJECTCLASS;

/**
 * Represents a collection of helpers.
 * <p>
 *     You should not use this class directly in your code.
 * </p>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Internal
public final class Utils {
    @SuppressWarnings("unchecked")
    private static final Function CALL_SILENT_FN;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodType lambdaSignature = MethodType.methodType(Object.class, Callable.class);

        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    lookup.findStatic(Utils.class, "callUncheckedImpl", lambdaSignature),
                    lambdaSignature);

            CALL_SILENT_FN = (Function) site.getTarget().invokeExact();
        } catch (final Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Utils(){
        throw new InstantiationError();
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

    public static <T, E extends Throwable> T callAndWrapException(final Callable<T> task, final Function<? super Exception, ? extends E> wrapper) throws E{
        try {
            return task.call();
        } catch (final Exception e) {
            throw wrapper.apply(e);
        }
    }

    private static Supplier<?> reflectGetter(final MethodHandles.Lookup lookup,
                                          final Object owner,
                                          final MethodHandle getter) throws ReflectiveOperationException {
        final MethodType invokedType = owner == null ?
                MethodType.methodType(Supplier.class) :
                MethodType.methodType(Supplier.class, owner.getClass());

        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup, "get",
                    invokedType,
                    MethodType.methodType(Object.class),
                    getter,
                    MethodType.methodType(getter.type().returnType()));
            return (Supplier<?>) (owner == null ? site.getTarget().invoke() : site.getTarget().invoke(owner));
        } catch (final LambdaConversionException e){
            throw new ReflectiveOperationException(e);
        } catch (final Throwable e){
            throw new InvocationTargetException(e);
        }
    }

    public static Supplier<?> reflectGetter(final MethodHandles.Lookup lookup,
                                                            final Object owner,
                                                            final Method getter) throws ReflectiveOperationException {
        return reflectGetter(lookup, owner, lookup.unreflect(getter));
    }

    private static Consumer reflectSetter(final MethodHandles.Lookup lookup,
                                                            final Object owner,
                                                            final MethodHandle setter) throws ReflectiveOperationException {
        final MethodType invokedType;
        final MethodType instantiatedMethodType;
        if(owner == null){
            invokedType = MethodType.methodType(Consumer.class);
            instantiatedMethodType = MethodType.methodType(void.class, setter.type().parameterType(0));
        }
        else {
            invokedType = MethodType.methodType(Consumer.class, owner.getClass());
            instantiatedMethodType = MethodType.methodType(void.class, setter.type().parameterType(1));//zero index points to 'this' reference
        }
        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "accept",
                    invokedType,
                    MethodType.methodType(void.class, Object.class),
                    setter,
                    instantiatedMethodType);
            return (Consumer) (owner == null ? site.getTarget().invoke() : site.getTarget().invoke(owner));
        } catch (final LambdaConversionException e) {
            throw new ReflectiveOperationException(e);
        } catch (final Throwable e) {
            throw new InvocationTargetException(e);
        }
    }

    public static Consumer reflectSetter(final MethodHandles.Lookup lookup,
                                                               final Object owner,
                                                               final Method setter) throws ReflectiveOperationException {
        return reflectSetter(lookup, owner, lookup.unreflect(setter));
    }

    private static <T> Runnable runnableForEach(final Spliterator<T> spliterator, final Consumer<? super T> action){
        return () -> spliterator.forEachRemaining(action);
    }

    public static <T> void parallelForEach(final Spliterator<T> spliterator,
                                                                       final Consumer<? super T> action,
                                                                       final Executor threadPool) {
        Spliterator<T> subset = spliterator.trySplit();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() && subset != null; i++, subset = spliterator.trySplit())
            threadPool.execute(runnableForEach(subset, action));
        threadPool.execute(runnableForEach(spliterator, action));
    }

    public static <T> void parallelForEach(final Iterable<T> collection,
                                              final Consumer<? super T> action,
                                              final Executor threadPool){
        parallelForEach(collection.spliterator(), action, threadPool);
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    private static Object callUncheckedImpl(final Callable<?> callable) throws Exception{
        return callable.call();
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
    @SuppressWarnings("unchecked")
    public static <V> V callUnchecked(final Callable<V> callable){
        return (V) CALL_SILENT_FN.apply(callable);
    }

    /**
     * Closes many resources in guaranteed manner.
     * @param resources A set of resources to close.
     * @throws Exception One or more resource throw exception when closing.
     */
    public static void closeAll(final AutoCloseable... resources) throws Exception {
        Exception e = null;
        for (final AutoCloseable closeable : resources)
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
