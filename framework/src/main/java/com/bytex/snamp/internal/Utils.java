package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.Internal;
import com.google.common.base.Joiner;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;

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

    private static final Supplier NULL_SUPPLIER = Suppliers.ofInstance(null);

    private Utils(){

    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> nullSupplier(){
        return NULL_SUPPLIER;
    }

    private static String getOS(){
        return StandardSystemProperty.OS_NAME.value();
    }

    public static String getFullyQualifiedResourceName(final Class<?> locator, String name){
        if(locator.isArray())
            return getFullyQualifiedResourceName(locator.getComponentType(), name);
        else if (!name.startsWith("/")) {
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
    public static <T> T safeCast(final Object obj, final Class<T> resultType) {
        try {
            return resultType.cast(obj);
        } catch (final ClassCastException e) {
            return null;
        }
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
        if(defaultValue == null) return getProperty(map, propertyKey, propertyType, Utils.<V>nullSupplier());
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
        if(defaultValue == null) return getProperty(dict, propertyKey, propertyType, Utils.<V>nullSupplier());
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

    public static <K, V> boolean mapsAreEqual(final Map<K, V> map1,
                                              final Map<K, V> map2){
        if(map1 == null) return map2 == null;
        else
            return map2 != null && map1.size() == map2.size() && map1.entrySet().containsAll(map2.entrySet());
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

    public static Properties toProperties(final Map<String, String> params){
        final Properties props = new Properties();
        props.putAll(params);
        return props;
    }

    public static <A extends Annotation> A getParameterAnnotation(final Method method,
                                                                  final int parameterIndex,
                                                                  final Class<A> annotationType) {
        final Annotation[][] annotations = method.getParameterAnnotations();
        if(annotations.length >= parameterIndex)
            return null;
        for(final Annotation candidate: annotations[parameterIndex])
            if(annotationType.isInstance(candidate))
                return annotationType.cast(candidate);
        return null;
    }
}
