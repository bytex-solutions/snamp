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
 * @version 1.2
 */
public abstract class AbstractAggregator implements Aggregator {
    private interface AggregationSupplier {
        Object get(final Aggregator owner) throws ReflectiveOperationException;
    }

    private static final class FieldAggregationSupplier implements AggregationSupplier {
        private final Field field;

        private FieldAggregationSupplier(final Field fld){
            this.field = Objects.requireNonNull(fld);
        }

        @Override
        public Object get(final Aggregator owner) throws IllegalAccessException {
            return field.get(owner);
        }
    }

    private static final class MethodAggregationSupplier implements AggregationSupplier {
        private final Method method;

        private MethodAggregationSupplier(final Method m){
            this.method = Objects.requireNonNull(m);
        }

        @Override
        public Object get(final Aggregator owner) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(owner);
        }
    }

    private static final class ObjectAggregationSupplier implements AggregationSupplier{
        private final Object object;

        private ObjectAggregationSupplier(final Object obj){
            this.object = Objects.requireNonNull(obj);
        }

        @Override
        public Object get(final Aggregator owner) {
            return object;
        }
    }

    private static final class AggregationNotFoundException extends Exception{
        private static final long serialVersionUID = -6675396043935484212L;

        private AggregationNotFoundException(){

        }
    }

    private static final class AggregationCacheLoader extends CacheLoader<Class<?>, AggregationSupplier>{
        private final Class<? extends Aggregator> aggregatorType;

        private AggregationCacheLoader(final Class<? extends Aggregator> declaredType){
            this.aggregatorType = declaredType;
        }

        private static AggregationSupplier load(final Class<?> inheritanceFrame, final Class<?> serviceType) {
            //iterates through fields
            for (final Field f : inheritanceFrame.getDeclaredFields())
                if (f.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(f.getType())) {
                    if (!f.isAccessible()) f.setAccessible(true);
                    return new FieldAggregationSupplier(f);
                }
            //iterates through methods
            for (final Method m : inheritanceFrame.getDeclaredMethods())
                if (m.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(m.getReturnType())) {
                    if (!m.isAccessible()) m.setAccessible(true);
                    return new MethodAggregationSupplier(m);
                }
            return null;
        }

        @Override
        public AggregationSupplier load(final Class<?> serviceType) throws AggregationNotFoundException {
            for (Class<?> inheritanceFrame = aggregatorType; !inheritanceFrame.equals(AbstractAggregator.class); inheritanceFrame = inheritanceFrame.getSuperclass()) {
                final AggregationSupplier result = load(inheritanceFrame, serviceType);
                if (result != null) return result;
            }
            throw new AggregationNotFoundException();
        }
    }

    /**
     * Represents aggregation builder.
     * This class cannot be inherited or instantiated directly from your code.
     */
    public static final class AggregationBuilder implements Supplier<AbstractAggregator>{
        private AbstractAggregator aggregator;

        private AggregationBuilder(){

        }

        private static AbstractAggregator createAggregator(){
            return new AbstractAggregator() {};
        }

        public <T> AggregationBuilder aggregate(final Class<T> objectType, final T obj) {
            if (aggregator == null)
                aggregator = createAggregator();
            aggregator.providers.put(objectType, new ObjectAggregationSupplier(obj));
            return this;
        }

        @Override
        public AbstractAggregator get() {
            final AbstractAggregator result = this.aggregator;
            this.aggregator = null;
            return result == null ? createAggregator() : result;
        }

        public Aggregator build(){
            return get();
        }
    }

    private final LoadingCache<Class<?>, AggregationSupplier> providers;

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
     * @version 1.2
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
            return objectType.cast(providers.get(objectType).get(this));
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
