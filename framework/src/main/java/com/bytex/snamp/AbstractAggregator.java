package com.bytex.snamp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

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

    private static final class AggregationNotFoundException extends Exception{
        private static final long serialVersionUID = -6675396043935484212L;

        private AggregationNotFoundException(final Class<?> expectedType){
            super(String.format("Type '%s' is not supported by aggregator", expectedType));
        }
    }

    private static final class AggregationException extends IllegalStateException{
        private static final long serialVersionUID = 1626738914147286451L;

        private AggregationException(final Throwable cause){
            super(cause);
        }
    }

    private static class AggregationCacheLoader extends CacheLoader<Class<?>, AggregationSupplier>{
        private final Class<? extends Aggregator> aggregatorType;

        private AggregationCacheLoader(final Class<? extends Aggregator> declaredType){
            this.aggregatorType = declaredType;
        }

        private static AggregationSupplier reflectField(final Field fld){
            return new FieldAggregationSupplier(fld);
        }

        private static AggregationSupplier reflectMethod(final Method m) {
            return new MethodAggregationSupplier(m);
        }

        private static AggregationSupplier load(final Class<?> inheritanceFrame, final Class<?> serviceType) {
            //iterates through fields
            for (final Field f : inheritanceFrame.getDeclaredFields())
                if (f.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(f.getType())) {
                    if (!f.isAccessible()) f.setAccessible(true);
                    return reflectField(f);
                }
            //iterates through methods
            for (final Method m : inheritanceFrame.getDeclaredMethods())
                if (m.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(m.getReturnType())) {
                    if (!m.isAccessible()) m.setAccessible(true);
                    return reflectMethod(m);
                }
            return null;
        }

        @Override
        public AggregationSupplier load(final Class<?> serviceType) throws AggregationNotFoundException {
            for (Class<?> inheritanceFrame = aggregatorType; !inheritanceFrame.equals(AbstractAggregator.class); inheritanceFrame = inheritanceFrame.getSuperclass()) {
                final AggregationSupplier result = load(inheritanceFrame, serviceType);
                if (result != null) return result;
            }
            throw new AggregationNotFoundException(serviceType);
        }
    }

    private static final class FastAggregationCacheLoader extends AggregationCacheLoader{
        private final ImmutableMap<Class<?>, Callable<?>> predefinedSuppliers;

        private FastAggregationCacheLoader(final Class<? extends Aggregator> declaredType,
                                           final ImmutableMap<Class<?>, Callable<?>> predefinedSuppliers){
            super(declaredType);
            this.predefinedSuppliers = Objects.requireNonNull(predefinedSuppliers);
        }

        private static AggregationSupplier createAggregationSupplier(final Callable<?> provider) {
            return owner -> {
                try {
                    return provider.call();
                } catch (final Exception e) {
                    throw new InvocationTargetException(e);
                }
            };
        }

        @Override
        public AggregationSupplier load(final Class<?> serviceType) throws AggregationNotFoundException {
            Callable<?> provider = null;
            //check exact match
            if (predefinedSuppliers.containsKey(serviceType)) {
                provider = predefinedSuppliers.get(serviceType);
            } else {    //find suitable class in the map
                for (final Map.Entry<Class<?>, Callable<?>> entry : predefinedSuppliers.entrySet())
                    if (serviceType.isAssignableFrom(entry.getKey())) {
                        provider = entry.getValue();
                        break;
                    }
            }
            return provider == null ? super.load(serviceType) : createAggregationSupplier(provider);
        }
    }

    /**
     * Represents builder for user-defined aggregations.
     * This class cannot be inherited or instantiated directly from your code.
     * @since 1.2
     */
    protected static final class AggregationBuilder {
        private final ImmutableMap.Builder<Class<?>, Callable<?>> aggregations;

        private AggregationBuilder(){
            aggregations = ImmutableMap.builder();
        }

        public <T> AggregationBuilder add(final Class<T> objectType, final Callable<? extends T> supplier) {
            aggregations.put(objectType, Objects.requireNonNull(supplier));
            return this;
        }

        public <T> AggregationBuilder fromSupplier(final Class<T> objectType, final Supplier<? extends T> supplier) {
            return add(objectType, ExceptionalCallable.fromSupplier(supplier));
        }

        public <T> AggregationBuilder fromValue(final Class<T> objectType, final T obj) {
            if (obj == null) throw new NullPointerException("obj is null");
            return add(objectType, () -> obj);
        }

        private AggregationCacheLoader build(final Class<? extends Aggregator> aggregatorClass) {
            final ImmutableMap<Class<?>, Callable<?>> a = aggregations.build();
            return a.isEmpty() ?
                    new AggregationCacheLoader(aggregatorClass) :
                    new FastAggregationCacheLoader(aggregatorClass, a);
        }
    }

    private final LoadingCache<Class<?>, AggregationSupplier> providers;

    /**
     * Initializes a new aggregator which uses reflection on the new instance to discover aggregated objects.
     * <p>
     *     A necessary program elements (fields, methods) should be marked with {@link Aggregation} annotation.
     *  @see Aggregation
     */
    protected AbstractAggregator() {
        final AggregationBuilder builder = new AggregationBuilder();
        registerExtraAggregations(builder);
        providers = CacheBuilder.newBuilder().build(builder.build(getClass()));
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
            if (e.getCause() instanceof AggregationNotFoundException)
                return queryObjectFallback(objectType, fallback);  //try fallback scenarios
            else throw new AggregationException(e.getCause());
        } catch (final ReflectiveOperationException e) {
            throw new AggregationException(e);
        }
    }

    public static <T1, T2> AbstractAggregator of(final Class<T1> firstType,
                                                     final Callable<? extends T1> first,
                                                     final Class<T2> secondType,
                                                     final Callable<? extends T2> second) {
        return new AbstractAggregator() {
            @Override
            protected void registerExtraAggregations(final AggregationBuilder customAggregations) {
                customAggregations
                        .add(firstType, first)
                        .add(secondType, second);
            }
        };
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
}
