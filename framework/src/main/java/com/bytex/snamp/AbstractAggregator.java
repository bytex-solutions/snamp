package com.bytex.snamp;

import com.google.common.base.Supplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

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
    private static final class SimpleAggregator extends Switch<Class<?>, Object> implements Aggregator{
        private static final class IsAssignableFromCase<T> extends CaseStatement<Class<?>, Object>{
            private final Class<T> aggregatedObjectType;
            private final T aggregatedObject;

            private IsAssignableFromCase(final Class<T> et, final T obj){
                this.aggregatedObjectType = Objects.requireNonNull(et);
                this.aggregatedObject = Objects.requireNonNull(obj);
            }

            @Override
            public boolean match(final Class<?> expectedType) {
                return expectedType.isAssignableFrom(aggregatedObjectType);
            }

            @Override
            public T apply(final Class<?> expectedType) {
                return aggregatedObject;
            }
        }

        private <T> void addCase(final Class<T> objectType, final T aggregatedObject){
            addCase(new IsAssignableFromCase<>(objectType, aggregatedObject));
        }

        @Override
        public <T> T queryObject(final Class<T> objectType) {
            final Object obj = apply(objectType);
            return objectType.isInstance(obj) ? objectType.cast(obj) : null;
        }
    }

    /**
     * Represents aggregation builder.
     * This class cannot be inherited.
     */
    public static final class AggregationBuilder implements Supplier<Aggregator>{
        private SimpleAggregator aggregator;

        private AggregationBuilder(){

        }

        public <T> AggregationBuilder aggregate(final Class<T> objectType, final T obj){
            if(aggregator == null)
                aggregator = new SimpleAggregator();
            aggregator.addCase(objectType, obj);
            return this;
        }

        @Override
        public Aggregator get() {
            final Aggregator result = this.aggregator;
            this.aggregator = null;
            return result == null ? new SimpleAggregator() : result;
        }

        public Aggregator build(){
            return get();
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

    protected final <T> T queryObject(final Class<T> objectType, final Aggregator fallback) {
        //iterates through all derived classes
        Class<?> lookup = getClass();
        while (lookup != null) {
            final T serviceInstance = queryObject(lookup, objectType);
            if (serviceInstance == null) lookup = lookup.getSuperclass();
            else return serviceInstance;
        }
        //try fallback scenario
        if(objectType.isInstance(this))
            return objectType.cast(this);
        else if(fallback != null) return fallback.queryObject(objectType);
        else return null;
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
        return queryObject(objectType, (Aggregator) null);
    }

    /**
     * Constructs a new aggregation builder.
     * @return A new instance of the aggregation builder.
     */
    public static AggregationBuilder builder(){
        return new AggregationBuilder();
    }

    public static Aggregator compose(final Aggregator... values){
        return new Aggregator() {
            @Override
            public <T> T queryObject(final Class<T> objectType) {
                for(final Aggregator a: values){
                    final T result = a.queryObject(objectType);
                    if(result != null) return result;
                }
                return null;
            }
        };
    }
}
