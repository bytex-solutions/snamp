package com.bytex.snamp;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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
     * This class cannot be inherited or instantiated directly from your code.
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

    private interface AggregationProvider{
        <T> T get(final Aggregator owner, final Class<T> expectedType) throws ReflectiveOperationException;
    }

    private static final class AggregationNotFoundException extends Exception{
        private static final long serialVersionUID = -6675396043935484212L;

        private AggregationNotFoundException(){

        }
    }

    private static final class AggregationCacheLoader extends CacheLoader<Class<?>, AggregationProvider>{
        private final Class<? extends Aggregator> aggregatorType;

        private AggregationCacheLoader(final Class<? extends Aggregator> declaredType){
            this.aggregatorType = declaredType;
        }

        private static AggregationProvider createAggregationProvider(final Field fld){
            return new AggregationProvider() {
                @Override
                public <T> T get(final Aggregator owner, final Class<T> expectedType) throws IllegalAccessException {
                    return expectedType.cast(fld.get(owner));
                }
            };
        }

        private static AggregationProvider createAggregationProvider(final Method m) {
            return new AggregationProvider() {
                @Override
                public <T> T get(final Aggregator owner, final Class<T> expectedType) throws InvocationTargetException, IllegalAccessException {
                    return expectedType.cast(m.invoke(owner));
                }
            };
        }

        private static AggregationProvider load(final Class<?> inheritanceFrame, final Class<?> serviceType) throws AggregationNotFoundException {
            //iterates through fields
            for (final Field f : inheritanceFrame.getDeclaredFields())
                if (f.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(f.getType())) {
                    if (!f.isAccessible()) f.setAccessible(true);
                    return createAggregationProvider(f);
                }
            //iterates through methods
            for (final Method m : inheritanceFrame.getDeclaredMethods())
                if (m.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(m.getReturnType())) {
                    if (!m.isAccessible()) m.setAccessible(true);
                    return createAggregationProvider(m);
                }
            throw new AggregationNotFoundException();
        }

        @Override
        public AggregationProvider load(final Class<?> serviceType) throws AggregationNotFoundException {
            return load(aggregatorType, serviceType);
        }
    }

    private final LoadingCache<Class<?>, AggregationProvider> providers;

    protected AbstractAggregator() {
        providers = CacheBuilder.newBuilder().build(new AggregationCacheLoader(getClass()));
    }

    /**
     * Clears internal cache with aggregated objects.
     */
    protected final void clearCache(){
        providers.invalidateAll();
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

    private <T> T queryObjectFallback(final Class<T> objectType, final Aggregator fallback) {
        if (objectType.isInstance(this))
            return objectType.cast(this);
        else if (fallback != null)
            return fallback.queryObject(objectType);
        else
            return null;
    }

    protected final <T> T queryObject(final Class<T> objectType, final Aggregator fallback) {
        try {
            //try to load from cache
            return providers.get(objectType).get(this, objectType);
        } catch (final ExecutionException e) {
            return e.getCause() instanceof AggregationNotFoundException ?
                    queryObjectFallback(objectType, fallback) :  //try fallback scenarios
                    null;
        } catch (final ReflectiveOperationException e) {
            return null;
        }
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
        return queryObject(objectType, null);
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
