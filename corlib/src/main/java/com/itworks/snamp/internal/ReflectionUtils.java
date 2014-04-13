package com.itworks.snamp.internal;

import org.apache.commons.collections4.Factory;
import org.osgi.framework.*;
import static org.osgi.framework.Constants.OBJECTCLASS;

import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.Objects;

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
     * Creates transparent weak reference to the specified object.
     * @param obj An object to be referenced.
     * @param iface Superclass or interface implemented by the specified object.
     * @param <I>
     * @param <T>
     * @return
     */
    public static <I, T extends I> I weakReference(final T obj, final Class<I> iface){
        return wrapReference(new WeakReference<>(obj), iface);
    }

    public static <I, T extends I> I weakReference(final Factory<T> activator, final Class<I> iface){
        return weakReference(activator.create(), iface);
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

    public static String getFullyQualifiedResourceName(Class<?> locator, String name){
        if (!name.startsWith("/")) {
            while (locator.isArray())
                locator = locator.getComponentType();
            final String baseName = locator.getName();
            final int index = baseName.lastIndexOf('.');
            if (index != -1)
                name = String.format("%s/%s", baseName.substring(0, index).replace('.', '/'), name);
        }
        else name = name.substring(1);
        return name;
    }

    /**
     * Provides safe typecast.
     * @param obj An object to cast.
     * @param resultType Type of the cast operation. Cannot be {@literal null}.
     * @param <T> Type of the cast operation.
     * @return Cast result; or {@literal null}, if the specified object is not instance
     * of the specified type.
     */
    public static <T> T safeCast(final Object obj, final Class<T> resultType){
        return obj != null && resultType.isInstance(obj) ? resultType.cast(obj) : null;
    }

    /**
     * Returns a bundle context associated with bundle which owns the specified object.
     * @param obj An object to be used for context resolving.
     * @return A bundle context associated with bundle which owns the specified object; or {@literal null}
     * if bundle context cannot be resolved.
     */
    public static BundleContext getBundleContextByObject(final Object obj){
        return obj != null ? FrameworkUtil.getBundle(obj.getClass()).getBundleContext() : null;
    }

    public static boolean isInstanceOf(final ServiceReference<?> serviceRef, final String serviceType){
        final Object names = serviceRef.getProperty(OBJECTCLASS);
        if(names != null && names.getClass().isArray())
            for(int i = 0; i < Array.getLength(names); i++)
                if(Objects.equals(Array.get(names, i), serviceType)) return true;
        return false;
    }

    public static boolean isInstanceOf(final ServiceReference<?> serviceRef, final Class<?> serviceType){
        return isInstanceOf(serviceRef, serviceType.getName());
    }
}
