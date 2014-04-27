package com.itworks.snamp;

import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * Represents a base class for all aggregators.
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
        //iterates through all derived classes
        Class<?> lookup = getClass();
        while (lookup != null){
            final T serviceInstance = queryObject(lookup, objectType);
            if(serviceInstance == null) lookup = lookup.getSuperclass();
            else return serviceInstance;
        }
        return objectType.isInstance(this) ? objectType.cast(this) : null;
    }
}
