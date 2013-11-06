package com.snamp;

import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * @author roman
 */
public abstract class AbstractAggregated implements Aggregator {
    /**
     * Identifies that the parameterless method or field holds the aggregated object (service).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    protected @interface Aggregation{

    }

    private final <T> T queryObject(final Class<?> inheritanceFrame, final Class<T> serviceType){
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
     * Retrieves the service instance.
     *
     * @param objectType Type of the requested service.
     * @param <T>         Type of the required service.
     * @return An instance of the requested service; or {@literal null} if service is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        //iterates through all derived classes
        Class<?> lookup = getClass();
        while (lookup != null){
            final T serviceInstance = queryObject(lookup, objectType);
            if(serviceInstance == null) lookup = lookup.getSuperclass();
            else return serviceInstance;
        }
        return null;
    }
}
