package com.snamp;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Represents advanced reflection subroutines.
 * <p>
 *     You should not use this class directly in your code.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public final class ReflectionUtils {
    private ReflectionUtils(){

    }

    /**
     * Isolates the interface reference from the implementation class.
     * @param obj An implementation of the interface.
     * @param iface An interface to isolate.
     * @param weakIsolation {@literal true}, if returned interface reference has weak reference to its implementer;
     * {@literal false}, if returned interface reference has strong reference to its implementer.
     * @param <I> Type of the interface to isolate.
     * @param <T> The class that implements the interface to isolate.
     * @return A reference to the implemented interface that cannot be casted to implementer.
     * @throws java.lang.IllegalArgumentException iface is not an interface.
     */
    public static  <I, T extends I> I isolate(final T obj, final Class<I> iface, final boolean weakIsolation){

        if(obj == null) return null;
        else if(weakIsolation){
            final Reference<T> weakObj = new WeakReference<>(obj);
            return iface.cast(Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
                @Override
                public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return method.invoke(weakObj.get(), args);
                }
            }));
        }
        else return iface.cast(Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
            @Override
            public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return method.invoke(obj, args);
            }
        }));
    }

    /**
     * Isolates the interface reference from the implementation class.
     * @param obj An implementation of the interface.
     * @param iface An interface to isolate.
     * @param <I> Type of the interface to isolate.
     * @param <T> The class that implements the interface to isolate.
     * @return A reference to the implemented interface that cannot be casted to implementer.
     * @throws java.lang.IllegalArgumentException iface is not an interface.
     */
    public static  <I, T extends I> I isolate(final T obj, final Class<I> iface){
        return isolate(obj, iface, false);
    }

    /**
     * Wraps the reference to an object into the specified interface.
     * @param obj A reference to wrap.
     * @param iface An information about wrapper type.
     * @param <I> Method return type.
     * @param <T> Type of the referenced object.
     * @return A strong reference wrapper for the specified reference (soft, weak, phantom).
     */
    public static <I, T extends I> I wrapReference(final Reference<T> obj, final Class<I> iface){
        return iface.cast(Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
            @Override
            public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return method.invoke(obj.get(), args);
            }
        }));
    }

    /**
     * Casts the specified object to a another class and isolates interface
     * from the conversion result.
     * @param obj An object to cast. Cannot be {@literal null}.
     * @param castResult Type of the conversion result.
     * @param iface An interface to isolate.
     * @param <I> Type of the interface to isolate.
     * @param <T> Type of the conversion result.
     * @return Isolated reference to an interface.
     */
    public static <I, T extends I> I castAndIsolate(final Object obj, final Class<T> castResult, final Class<I> iface){
        return isolate(castResult.cast(obj), iface);
    }
}
