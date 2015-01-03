package com.itworks.snamp.internal;

import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.Wrapper;
import com.itworks.snamp.internal.annotations.Internal;
import org.osgi.framework.*;
import org.osgi.service.event.Event;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.Objects;

import static org.osgi.framework.Constants.OBJECTCLASS;

/**
 * Represents a collection of helpers.
 * <p>
 *     You should not use this class directly in your code.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public final class Utils {
    private static final class WeakInvocationHandler<T> extends WeakReference<T> implements InvocationHandler, Wrapper<T>{
        private WeakInvocationHandler(final T obj){
            super(obj);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return method.invoke(get(), args);
        }

        @Override
        public <R> R handle(final Function<T, R> handler) {
            return handler.apply(get());
        }
    }

    private static final class SoftInvocationHandler<T> extends SoftReference<T> implements InvocationHandler, Wrapper<T>{
        private SoftInvocationHandler(final T obj){
            super(obj);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return method.invoke(get(), args);
        }

        @Override
        public <R> R handle(final Function<T, R> handler) {
            return handler.apply(get());
        }
    }

    private static final class FinalizeDelegator<T> implements InvocationHandler, Wrapper<T> {
        private final T obj;
        private final Consumer<T, ?> finalizer;

        private FinalizeDelegator(final T obj,
                                  final Consumer<T, ?> finalizer) {
            this.obj = Objects.requireNonNull(obj);
            this.finalizer = Objects.requireNonNull(finalizer);
        }

        @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
        @Override
        protected void finalize() throws Throwable {
            finalizer.accept(obj);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return method.invoke(obj, args);
        }

        @Override
        public <R> R handle(final Function<T, R> handler) {
            return handler.apply(obj);
        }
    }

    /**
     * Determines whether the underlying OS is Linux.
     */
    public static final boolean IS_OS_LINUX = getOS().startsWith("LINUX") || getOS().startsWith("Linux");

    /**
     * Determines whether the underlying OS is MacOS X.
     */
    public static final boolean IS_OS_MAC_OSX = getOS().startsWith("Mac OS X");

    /**
     * Determines whether the underlying OS is Windows.
     */
    public static final boolean IS_OS_WINDOWS = getOS().startsWith("Windows");

    private Utils(){

    }

    private static String getOS(){
        return StandardSystemProperty.OS_NAME.value();
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
            return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
                @Override
                public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return method.invoke(weakObj.get(), args);
                }
            }));
        }
        else return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
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

    private static <I, T extends I, R extends Reference<T> & InvocationHandler> I wrapReference(final R obj, final Class<I> iface){
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, obj));
    }

    public static <I, T extends I> I weakReference(final T obj, final Class<I> iface) {
        return wrapReference(new WeakInvocationHandler<>(obj), iface);
    }

    public static <I, T extends I> I softReference(final T obj, final Class<I> iface){
        return wrapReference(new SoftInvocationHandler<>(obj), iface);
    }

    public static <I, T extends I> I overrideFinalize(final T obj,
                                                      final Class<I> iface,
                                                      final Consumer<T, ?> finalizer){
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(),
                new Class<?>[]{iface},
                new FinalizeDelegator<>(obj, finalizer)));
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
    public static BundleContext getBundleContextByObject(final Object obj) {
        return obj != null ?
                FrameworkUtil.getBundle(obj.getClass()).getBundleContext():
                null;
    }

    /**
     * Determines whether the service implements the specified interface.
     * @param serviceRef The service reference to check.
     * @param serviceType The name of the interface.
     * @return
     */
    public static boolean isInstanceOf(final ServiceReference<?> serviceRef, final String serviceType){
        final Object names = serviceRef.getProperty(OBJECTCLASS);
        if(names != null && names.getClass().isArray())
            for(int i = 0; i < Array.getLength(names); i++)
                if(Objects.equals(Array.get(names, i), serviceType)) return true;
        return false;
    }

    /**
     * Determines whether the service implements the specified interface.
     * @param serviceRef
     * @param serviceType
     * @return
     */
    public static boolean isInstanceOf(final ServiceReference<?> serviceRef, final Class<?> serviceType){
        return isInstanceOf(serviceRef, serviceType.getName());
    }

    /**
     * Gets value from the map in type-safe manner.
     * @param map The map to read. Cannot be {@literal null}.
     * @param propertyKey The key in the map to get.
     * @param propertyType The expected value type.
     * @param <K> Type of the map key.
     * @param <V> The expected value type.
     * @return Strongly typed value obtained from the map.
     * @throws ClassCastException Unable to cast property value to the specified propertyType.
     * @throws IndexOutOfBoundsException The specified key doesn't exist.
     * @throws IllegalArgumentException map is {@literal null}.
     */
    public static <K, V> V getProperty(final Map<K, ?> map,
                                       final K propertyKey,
                                       final Class<V> propertyType)
            throws ClassCastException,
                IndexOutOfBoundsException,
                IllegalArgumentException{
        if(map == null) throw new IllegalArgumentException("map is null.");
        else if(map.containsKey(propertyKey)){
            final Object value = map.get(propertyKey);
            if(propertyType.isInstance(value)) return propertyType.cast(value);
            else throw new ClassCastException(String.format("Unable to cast %s value to %s type.", value, propertyType));
        }
        else throw new IndexOutOfBoundsException(String.format("Key %s doesn't exist.", propertyKey));
    }

    /**
     * Gets value from the map in type-safe manner.
     * @param map The map to read.
     * @param propertyKey The key in the map to get.
     * @param propertyType The expected value type.
     * @param defaultValue Default value returned from the method if key doesn't exist or value
     *                     has invalid type.
     * @param <K> Type of the map key.
     * @param <V> The expected value type.
     * @return Strongly typed value returned from the map.
     */
    public static <K, V> V getProperty(final Map<K, ?> map,
                                       final K propertyKey,
                                       final Class<V> propertyType,
                                       final V defaultValue){
        return getProperty(map, propertyKey, propertyType, Suppliers.ofInstance(defaultValue));
    }

    /**
     * Gets value from the map in type-safe manner.
     * @param map The map to read.
     * @param propertyKey The key in the map to get.
     * @param propertyType The expected value type.
     * @param defaultValue Default value returned from the method if key doesn't exist or value
     *                     has invalid type.
     * @param <K> Type of the map key.
     * @param <V> The expected value type.
     * @return Strongly typed value returned from the map.
     */
    public static <K, V> V getProperty(final Map<K, ?> map,
                                       final K propertyKey,
                                       final Class<V> propertyType,
                                       final Supplier<V> defaultValue){
        if(defaultValue == null) return getProperty(map, propertyKey, propertyType, Suppliers.<V>ofInstance(null));
        else if(map == null) return defaultValue.get();
        else if(map.containsKey(propertyKey)){
            final Object value = map.get(propertyKey);
            return propertyType.isInstance(value) ? propertyType.cast(value) : defaultValue.get();
        }
        else return defaultValue.get();
    }

    /**
     * Gets value from the dictionary in type-safe manner.
     * @param dict The dictionary to read.
     * @param propertyKey The key in the dictionary to get.
     * @param propertyType The expected value type.
     * @param defaultValue Default value returned from the method if key doesn't exist or value
     *                     has invalid type.
     * @param <K> Type of the dictionary key.
     * @param <V> The expected value type.
     * @return Strongly typed value returned from the dictionary.
     */
    public static <K, V> V getProperty(final Dictionary<K, ?> dict,
                                       final K propertyKey,
                                       final Class<V> propertyType,
                                       final Supplier<V> defaultValue){
        if(defaultValue == null) return getProperty(dict, propertyKey, propertyType, Suppliers.<V>ofInstance(null));
        else if(dict == null) return defaultValue.get();
        final Object value = dict.get(propertyKey);
        return value != null && propertyType.isInstance(value) ? propertyType.cast(value) : defaultValue.get();
    }

    public static <K, V> boolean setProperty(final Dictionary<K, ? super V> dict,
                                     final K propertyKey,
                                     final V value) {
        if (dict == null) return false;
        dict.put(propertyKey, value);
        return true;
    }

    /**
     * Gets value from the dictionary in type-safe manner.
     * @param dict The dictionary to read.
     * @param propertyKey The key in the dictionary to get.
     * @param propertyType The expected value type.
     * @param defaultValue Default value returned from the method if key doesn't exist or value
     *                     has invalid type.
     * @param <K> Type of the dictionary key.
     * @param <V> The expected value type.
     * @return Strongly typed value returned from the dictionary.
     */
    public static <K, V> V getProperty(final Dictionary<K, ?> dict,
                                     final K propertyKey,
                                     final Class<V> propertyType,
                                     final V defaultValue){
        return getProperty(dict, propertyKey, propertyType, Suppliers.ofInstance(defaultValue));
    }

    /**
     * Gets value of the Java Bean property.
     * @param obj An object that contains a property.
     * @param descriptor Descriptor of the object that contains a property.
     * @param propertyName The name of the property to get.
     * @return The value of the property.
     * @throws IntrospectionException Unable to get property value.
     */
    public static Object getProperty(final Object obj,
                                     final BeanInfo descriptor,
                                     final String propertyName) throws IntrospectionException, ReflectiveOperationException{
        if(obj == null) throw new IllegalArgumentException("obj is null.");
        else if(descriptor == null) throw new IllegalArgumentException("descriptor is null.");
        else for(final PropertyDescriptor pd: descriptor.getPropertyDescriptors())
                if(Objects.equals(propertyName, pd.getName())){
                    final Method getter = pd.getReadMethod();
                    if(getter == null) throw new IntrospectionException(String.format("Property %s has no getter", propertyName));
                    else return getter.invoke(obj);
                }
        throw new IntrospectionException(String.format("Property %s not found", propertyName));
    }

    /**
     * Converts functional interface with input and output parameter into functional interface
     * with input parameter only.
     * @param transformer The functional interface to convert.
     * @param <I> Type of the value to be transformed.
     * @param <O> Type of the transformation result.
     * @return {@link com.google.common.base.Function} representation of the transformer.
     */
    public static <I, O> TransformerClosure<I, O> toClosure(final Function<I, O> transformer) {
        return transformer != null ?
                new TransformerClosure<I, O>() {
                    @Override
                    public O apply(final I input) {
                        return transformer.apply(input);
                    }
                } : null;
    }

    /**
     * Processes a service provided by the calling bundle.
     * @param caller The caller class.
     * @param serviceType The contract of the exposed service.
     * @param filter Exposed service selector. May be {@literal null} or empty.
     * @param serviceInvoker The object that implements procession logic.
     * @param <S> The contract of the exposed service.
     * @param <E> Type of the exception that may be occurred in service invoker.
     * @return {@literal true}, if requested service resolved; otherwise, {@literal false}.
     * @throws InvalidSyntaxException Invalid service selector.
     * @throws E Service invoker internal error.
     */
    public static <S, E extends Throwable> boolean processExposedService(final Class<?> caller,
                                                 final Class<S> serviceType,
                                                 final String filter,
                                                 final Consumer<S, E> serviceInvoker) throws InvalidSyntaxException, E {
        return processExposedService(caller, serviceType, filter == null || filter.isEmpty() ? null : FrameworkUtil.createFilter(filter), serviceInvoker);
    }

    /**
     * Processes a service provided by the calling bundle.
     * @param caller The caller class.
     * @param serviceType The contract of the exposed service.
     * @param filter Exposed service selector. May be {@literal null} or empty.
     * @param serviceInvoker The object that implements procession logic.
     * @param <S> The contract of the exposed service.
     * @param <E> Type of the exception that can be thrown by service invoker.
     * @return {@literal true}, if requested service resolved; otherwise, {@literal false}.
     * @throws E Service invoker internal error.
     */
    public static <S, E extends Throwable> boolean processExposedService(final Class<?> caller,
                                                 final Class<S> serviceType,
                                                 final Filter filter,
                                                 final Consumer<S, E> serviceInvoker) throws E {
        final Bundle owner = FrameworkUtil.getBundle(caller);
        final ServiceReference<?>[] services = owner.getRegisteredServices();
        for (final ServiceReference<?> ref : services != null ? services : new ServiceReference<?>[0])
            if ((filter == null || filter.match(ref)) && isInstanceOf(ref, serviceType))
                try {
                    serviceInvoker.accept(serviceType.cast(owner.getBundleContext().getService(ref)));
                    return true;
                } finally {
                    owner.getBundleContext().ungetService(ref);
                }
        return false;
    }

    /**
     * Determines whether the specified service is registered in OSGI service registry.
     * @param callerClass The caller class.
     * @param serviceType The service contract.
     * @param filter The service filter. May be {@literal null}.
     * @return {@literal true}, if the specified service is registered in OSGI service registry.
     * @throws org.osgi.framework.InvalidSyntaxException Incorrect filter.
     */
    public static boolean isServiceRegistered(final Class<?> callerClass,
                                       final Class<?> serviceType,
                                       final String filter) throws InvalidSyntaxException {
        final Bundle bundle = FrameworkUtil.getBundle(callerClass);
        return bundle.getBundleContext().getServiceReferences(serviceType, filter).size() > 0;
    }

    public static <V, E extends Exception> V withContextClassLoader(final ClassLoader loader, final ExceptionalCallable<V, E> action) throws E{
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

    public static <T> boolean collectionsAreEqual(final Collection<T> collection1, final Collection<T> collection2) {
        if (collection1 == null) return collection2 == null;
        else
            return collection2 != null && collection1.size() == collection2.size() && collection1.containsAll(collection2);
    }

    public static <K, V> boolean mapsAreEqual(final Map<K, V> map1,
                                              final Map<K, V> map2){
        if(map1 == null) return map2 == null;
        else
            return map2 != null && map1.size() == map2.size() && map1.entrySet().containsAll(map2.entrySet());
    }

    /**
     * Reads event property.
     * @param ev An event to parse.
     * @param propertyName The name of the event property to read.
     * @param propertyType The type of the event property.
     * @param defaultValue The default value if the property is not available.
     * @param <T> Type of the property to read.
     * @return The value of the property; or default value.
     */
    public static <T> T getEventProperty(final Event ev,
                                         final String propertyName,
                                         final Class<T> propertyType,
                                         final T defaultValue){
        if(ev == null) return defaultValue;
        else if(ev.containsProperty(propertyName)){
            final Object result = ev.getProperty(propertyName);
            return propertyType.isInstance(result) ? propertyType.cast(result) : defaultValue;
        }
        else return defaultValue;
    }

    /**
     * Gets the stack trace in the form of the single string.
     * @return The current stack trace.
     */
    public static String getStackTrace(){
        return Joiner.on(System.lineSeparator()).join(Thread.currentThread().getStackTrace());
    }
}
