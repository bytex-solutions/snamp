package com.itworks.snamp;

import com.google.common.base.Function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a basic support for aggregation.
 * <p>
 *  You can use {@link Aggregation} annotation for annotating fields and parameterless methods. These
 *  program elements will be automatically recognized as aggregated object and available for requesting
 *  through {@link #queryObject(Class)} method.
 *  <b>Example:</b><br/>
 *  <pre><code>
 *  final class CustomAggregator extends AbstractAggregator{
 *      {@literal @}Aggregation
 *      private final File someFile;
 *
 *      public CustomAggregator(final String fileName){
 *          someFile = new File(fileName);
 *      }
 *  }
 *  </code></pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAggregator implements Aggregator {
    private static final class SimpleAggregator extends Switch<Class<?>, Object> implements Aggregator {

        @Override
        public <T> T queryObject(final Class<T> objectType) {
            return apply(objectType, objectType);
        }
    }

    /**
     * Identifies that the parameterless method or field holds the aggregated object.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    protected @interface Aggregation{

    }

    private <T> T queryObject(final Class<?> inheritanceFrame, final Class<T> serviceType){
        try{
            //iterates through fields
            for(final Field f: inheritanceFrame.getDeclaredFields())
                if(f.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(f.getType())){
                    if(!f.isAccessible()) f.setAccessible(true);
                    return serviceType.cast(f.get(this));
                }
            //iterates through methods
            for(final Method m: inheritanceFrame.getDeclaredMethods())
                if(m.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(m.getReturnType())){
                    if(!m.isAccessible()) m.setAccessible(true);
                    return serviceType.cast(m.invoke(this));
                }
        }
        catch (final ReflectiveOperationException e){
            return null;
        }
        return null;
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @param <T>         Type of the required object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        if(objectType == null) return null;
        else if(objectType.isInstance(this))
            return objectType.cast(this);
        //iterates through all derived classes
        Class<?> lookup = getClass();
        while (lookup != null){
            final T serviceInstance = queryObject(lookup, objectType);
            if(serviceInstance == null) lookup = lookup.getSuperclass();
            else return serviceInstance;
        }
        return null;
    }

    protected static <T> T findObject(final Class<T> objectType,
                                      final Function<? super Class<T>, ? extends T> fallback,
                                      final Object... candidates){
        for(final Object obj: candidates)
            if(objectType.isInstance(obj)) return objectType.cast(obj);
        return fallback.apply(objectType);
    }

    public static <T> SimpleAggregator create(final Class<T> objectClass,
                                        final T obj){
        return (SimpleAggregator)new SimpleAggregator().equals(objectClass, obj);
    }

    public static <T1, T2> SimpleAggregator create(final Class<T1> objectClass1,
                                             final T1 obj1,
                                             final Class<T2> objectClass2,
                                             final T2 obj2){
        return (SimpleAggregator)create(objectClass1, obj1)
                .equals(objectClass2, obj2);
    }

    public static <T1, T2, T3> SimpleAggregator create(final Class<T1> objectClass1,
                                                 final T1 obj1,
                                                 final Class<T2> objectClass2,
                                                 final T2 obj2,
                                                 final Class<T3> objectClass3,
                                                 final T3 obj3){
        return (SimpleAggregator)create(objectClass1, obj1, objectClass2, obj2)
                .equals(objectClass3, obj3);
    }

    public static <T1, T2, T3, T4> SimpleAggregator create(final Class<T1> objectClass1,
                                                 final T1 obj1,
                                                 final Class<T2> objectClass2,
                                                 final T2 obj2,
                                                 final Class<T3> objectClass3,
                                                 final T3 obj3,
                                                 final Class<T4> objectClass4,
                                                 final T4 obj4){
        return (SimpleAggregator)create(objectClass1, obj1, objectClass2, obj2, objectClass3, obj3)
                .equals(objectClass4, obj4);
    }
}
