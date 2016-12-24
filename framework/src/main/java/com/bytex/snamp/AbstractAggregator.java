package com.bytex.snamp;

import com.bytex.snamp.internal.InheritanceNavigator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

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
 * @version 2.0
 */
public abstract class AbstractAggregator implements Aggregator {
    private interface AggregationSupplier {
        Object get(final Aggregator owner) throws ReflectiveOperationException;
    }

    private static final class CallableAggregationSupplier implements AggregationSupplier{
        private final Callable<?> callable;

        private CallableAggregationSupplier(final Callable<?> c){
            this.callable = Objects.requireNonNull(c);
        }

        @Override
        public Object get(final Aggregator owner) throws InvocationTargetException {
            return callAndWrapException(callable, InvocationTargetException::new);
        }

        @Override
        public String toString() {
            return callable.toString();
        }
    }

    private static final class CachedAggregationSupplier implements AggregationSupplier{
        private volatile Object supplierOrCachedObject;

        private CachedAggregationSupplier(final AggregationSupplier supplier){
            this.supplierOrCachedObject = Objects.requireNonNull(supplier);
        }

        private synchronized Object cacheAndGet(final Aggregator owner) throws ReflectiveOperationException{
            if(supplierOrCachedObject instanceof AggregationSupplier)
                supplierOrCachedObject = ((AggregationSupplier) supplierOrCachedObject).get(owner);
            return supplierOrCachedObject;
        }

        @Override
        public Object get(final Aggregator owner) throws ReflectiveOperationException {
            return supplierOrCachedObject instanceof AggregationSupplier ? cacheAndGet(owner) : supplierOrCachedObject;
        }

        @Override
        public String toString() {
            return supplierOrCachedObject.toString();
        }
    }

    private static final class AggregationNotFoundException extends Exception{
        private static final long serialVersionUID = -6675396043935484212L;

        private AggregationNotFoundException(final Class<?> expectedType){
            super(String.format("Type '%s' is not supported by aggregator", expectedType));
        }

        @Override
        public AggregationNotFoundException fillInStackTrace() {     //do not collect stacktrace information to increase performance
            return this;
        }
    }

    private static final class AggregationException extends IllegalStateException{
        private static final long serialVersionUID = 1626738914147286451L;

        private AggregationException(final Throwable cause){
            super(cause);
        }
    }

    private static abstract class AggregationCacheLoader extends CacheLoader<Class<?>, AggregationSupplier>{
        @Override
        public abstract AggregationSupplier load(@Nonnull final Class<?> serviceType) throws AggregationNotFoundException;
    }

    private static final class ReflectionCacheLoader extends AggregationCacheLoader{
        private final InheritanceNavigator<? extends AbstractAggregator> aggregatorType;

        private ReflectionCacheLoader(final Class<? extends AbstractAggregator> declaredType){
            this.aggregatorType = InheritanceNavigator.of(declaredType, AbstractAggregator.class);
        }

        private static void setAccessibleIfNecessary(final AccessibleObject obj) {
            if (!obj.isAccessible())
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    obj.setAccessible(true);
                    return null;
                });
        }

        private static boolean isCached(final AnnotatedElement element){
            return element.getAnnotation(Aggregation.class).cached();
        }

        private static AggregationSupplier reflectField(final Field f) {
            setAccessibleIfNecessary(f);
            final AggregationSupplier supplier = f::get;
            return Modifier.isFinal(f.getModifiers()) || isCached(f) ? new CachedAggregationSupplier(supplier) : supplier;
        }

        private static AggregationSupplier reflectMethod(final Method m){
            setAccessibleIfNecessary(m);
            final AggregationSupplier supplier = m::invoke;
            return isCached(m) ? new CachedAggregationSupplier(supplier) : supplier;
        }

        private static AggregationSupplier load(final Class<?> inheritanceFrame, final Class<?> serviceType) {
            //iterates through fields
            for (final Field f : inheritanceFrame.getDeclaredFields())
                if (f.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(f.getType()))
                    return reflectField(f);
            //iterates through methods
            for (final Method m : inheritanceFrame.getDeclaredMethods())
                if (m.isAnnotationPresent(Aggregation.class) && serviceType.isAssignableFrom(m.getReturnType()))
                    return reflectMethod(m);
            return null;
        }

        @Override
        public AggregationSupplier load(@Nonnull final Class<?> serviceType) throws AggregationNotFoundException {
            for(final Class<?> inheritanceFrame: aggregatorType){
                final AggregationSupplier result = load(inheritanceFrame, serviceType);
                if (result != null) return result;
            }
            //detect whether the requested type is implemented by aggregator itself
            if (serviceType.isAssignableFrom(aggregatorType.getStartingClass()))
                return owner -> owner;
            throw new AggregationNotFoundException(serviceType);
        }
    }

    private static final class SingleTypeCacheLoader extends AggregationCacheLoader {
        private final Class<?> expectedType;
        private final CallableAggregationSupplier supplier;

        private SingleTypeCacheLoader(final Class<?> type, final Callable<?> callable) {
            this.expectedType = Objects.requireNonNull(type);
            this.supplier = new CallableAggregationSupplier(callable);
        }

        private SingleTypeCacheLoader(final Map.Entry<Class<?>, Callable<?>> entry){
            this(entry.getKey(), entry.getValue());
        }

        @Override
        public AggregationSupplier load(@Nonnull final Class<?> serviceType) throws AggregationNotFoundException {
            if (serviceType.isAssignableFrom(expectedType))
                return supplier;
            throw new AggregationNotFoundException(serviceType);
        }
    }

    private static final class FastAggregationCacheLoader extends AggregationCacheLoader{
        private final ImmutableMap<Class<?>, Callable<?>> predefinedSuppliers;

        private FastAggregationCacheLoader(final ImmutableMap<Class<?>, Callable<?>> predefinedSuppliers){
            this.predefinedSuppliers = Objects.requireNonNull(predefinedSuppliers);
        }

        @Override
        public AggregationSupplier load(@Nonnull final Class<?> serviceType) throws AggregationNotFoundException {
            //check exact match
            Callable<?> provider = predefinedSuppliers.get(serviceType);
            if (provider == null) {   //find suitable class in the map
                for (final Map.Entry<Class<?>, Callable<?>> entry : predefinedSuppliers.entrySet())
                    if (serviceType.isAssignableFrom(entry.getKey()))
                        return new CallableAggregationSupplier(entry.getValue());
            } else
                return new CallableAggregationSupplier(provider);
            throw new AggregationNotFoundException(serviceType);
        }
    }

    /**
     * Represents builder for user-defined aggregations.
     * This class cannot be inherited or instantiated directly from your code.
     * @since 1.2
     */
    public static final class AggregationBuilder implements Supplier<Aggregator> {
        private final ImmutableMap.Builder<Class<?>, Callable<?>> aggregations;

        private AggregationBuilder() {
            aggregations = ImmutableMap.builder();
        }

        public <T> AggregationBuilder add(final Class<T> objectType, final Callable<? extends T> supplier) {
            aggregations.put(objectType, Objects.requireNonNull(supplier));
            return this;
        }

        public <T> AggregationBuilder addSupplier(final Class<T> objectType, final Supplier<? extends T> supplier) {
            return add(objectType, supplier::get);
        }

        public <T> AggregationBuilder addValue(final Class<T> objectType, final T obj) {
            if (obj == null) throw new NullPointerException("obj is null");
            return add(objectType, () -> obj);
        }

        private static <I> AbstractAggregator createAnonymousAggregator(final I input, final Function<? super I, ? extends AggregationCacheLoader> loaderFactory) {
            final class DynamicAggregator extends AbstractAggregator {
                private DynamicAggregator(final I input, final Function<? super I, ? extends AggregationCacheLoader> loaderFactory) {
                    super(type -> loaderFactory.apply(input));
                }
            }
            return new DynamicAggregator(input, loaderFactory);
        }

        private static Aggregator build(final ImmutableMap<Class<?>, Callable<?>> aggregations) {
            switch (aggregations.size()) {
                case 0:
                    return EMPTY;
                case 1:
                    return createAnonymousAggregator(aggregations.entrySet().iterator().next(), SingleTypeCacheLoader::new);
                default:
                    return createAnonymousAggregator(aggregations, FastAggregationCacheLoader::new);
            }
        }

        @Override
        public Aggregator get() {
            return build(aggregations.build());
        }

        public Aggregator build() {
            return get();
        }
    }

    private final LoadingCache<Class<?>, AggregationSupplier> providers;

    private AbstractAggregator(final Function<Class<? extends AbstractAggregator>, ? extends AggregationCacheLoader> cacheLoader){
        providers = CacheBuilder.newBuilder().build(cacheLoader.apply(getClass()));
    }

    /**
     * Initializes a new aggregator which uses reflection on the new instance to discover aggregated objects.
     * <p>
     *     A necessary program elements (fields, methods) should be marked with {@link Aggregation} annotation.
     *  @see Aggregation
     */
    protected AbstractAggregator() {
        this(ReflectionCacheLoader::new);
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
     * @version 2.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    protected @interface Aggregation{
        /**
         * Determines whether the aggregation element can be cached.
         * @return {@literal true}, if element can be cached; otherwise, {@literal false}.
         */
        boolean cached() default false;
    }

    protected final <T> T queryObject(final Class<T> objectType, final Aggregator fallback) {
        try {
            //try to load from cache
            return objectType.cast(providers.get(objectType).get(this));
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof AggregationNotFoundException)
                return fallback.queryObject(objectType);
            else
                throw new AggregationException(e.getCause());
        } catch (final ReflectiveOperationException | ClassCastException e) {
            throw new AggregationException(e);
        }
    }

    public static AggregationBuilder builder(){
        return new AggregationBuilder();
    }

    @Override
    public final AbstractAggregator compose(final Aggregator other) {
        final class AggregatorComposition extends AbstractAggregator {
            private final Aggregator other;

            private AggregatorComposition(final Aggregator other) {
                this.other = Objects.requireNonNull(other);
            }

            @Override
            public <T> T queryObject(@Nonnull final Class<T> objectType) {
                final T obj = AbstractAggregator.this.queryObject(objectType);
                return obj == null ? other.queryObject(objectType) : obj;
            }
        }

        return new AggregatorComposition(other);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @param <T>         Type of the required object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return queryObject(objectType, EMPTY);
    }
}
