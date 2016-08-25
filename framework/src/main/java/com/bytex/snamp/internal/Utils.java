package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Internal;
import com.google.common.base.Joiner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
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
        return names instanceof Object[] && ArrayUtils.containsAny((Object[]) names, serviceType);
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

    public static <V> V withContextClassLoader(final ClassLoader loader, final Supplier<? extends V> action) {
        try {
            return withContextClassLoader(loader, (Callable<V>)action::get);
        } catch (final Exception e) {
            throw new AssertionError("Should never be happened", e);
        }
    }

    public static <V> V withContextClassLoader(final ClassLoader loader, final Callable<? extends V> action) throws Exception {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previous = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(loader);
        try{
            return action.call();
        }
        finally {
            currentThread.setContextClassLoader(previous);
        }
    }

    private static String getStackTrace(StackTraceElement[] stackTrace) {
        if (stackTrace.length > 0)
            stackTrace = ArrayUtils.remove(stackTrace, 0);
        return Joiner.on(System.lineSeparator()).join(stackTrace);
    }

    /**
     * Gets the stack trace in the form of the single string.
     * @return The current stack trace.
     */
    public static String getStackTrace(){
        return getStackTrace(Thread.currentThread().getStackTrace());
    }

    /**
     * Gets the stack trace in the form of the single string.
     * @param e An exception that holds its stack trace.
     * @return The stack trace associated with exception.
     */
    public static String getStackTrace(final Throwable e){
        return getStackTrace(e.getStackTrace());
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
    public static <T> T interfaceStaticInitialize(final Callable<T> initializer){
        try {
            return initializer.call();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static java.util.function.Supplier reflectGetter(final MethodHandles.Lookup lookup,
                                                               final Object owner,
                                                               final MethodHandle getter) throws ReflectiveOperationException {
        final MethodType invokedType = owner == null ?
                MethodType.methodType(java.util.function.Supplier.class) :
                MethodType.methodType(java.util.function.Supplier.class, owner.getClass());

        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup, "get",
                    invokedType,
                    MethodType.methodType(Object.class),
                    getter,
                    MethodType.methodType(getter.type().returnType()));
            return (java.util.function.Supplier<?>) (owner == null ? site.getTarget().invoke() : site.getTarget().invoke(owner));
        } catch (final LambdaConversionException e){
            throw new ReflectiveOperationException(e);
        } catch (final Throwable e){
            throw new InvocationTargetException(e);
        }
    }

    public static java.util.function.Supplier<?> reflectGetter(final MethodHandles.Lookup lookup,
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
            final CallSite site = LambdaMetafactory.metafactory(lookup, "accept",
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

    public static java.util.function.Consumer reflectSetter(final MethodHandles.Lookup lookup,
                                                               final Object owner,
                                                               final Method setter) throws ReflectiveOperationException {
        return reflectSetter(lookup, owner, lookup.unreflect(setter));
    }
}
