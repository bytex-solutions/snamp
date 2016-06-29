package com.bytex.snamp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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

    private static final class AggregationNotFoundException extends Exception{
        private static final long serialVersionUID = -6675396043935484212L;

        private AggregationNotFoundException(){

        }
    }

    private static final class AggregationException extends IllegalStateException{
        private static final long serialVersionUID = 1626738914147286451L;

        private AggregationException(final Throwable cause){
            super(cause);
        }
    }

    private static final class AggregationCacheLoader extends CacheLoader<Class<?>, AggregationSupplier>{
        private final Class<? extends Aggregator> aggregatorType;

        private AggregationCacheLoader(final Class<? extends Aggregator> declaredType){
            this.aggregatorType = declaredType;
        }

        private static AggregationSupplier reflectField(final Field fld){
            return new FieldAggregationSupplier(fld);
        }

        private static AggregationSupplier reflectMethod(final Method m) throws ReflectiveOperationException{
            final MethodType invokedType = MethodType.methodType(AggregationSupplier.class);
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                final CallSite site = LambdaMetafactory.metafactory(lookup, "get",
                        invokedType,
                        MethodType.methodType(Object.class, Aggregator.class),
                        lookup.unreflect(m),
                        MethodType.methodType(m.getReturnType(), m.getDeclaringClass()));
                return (AggregationSupplier) site.getTarget().invoke();
            } catch (final LambdaConversionException e) {
                throw new ReflectiveOperationException(e);
            } catch (final Throwable e) {
                throw new InvocationTargetException(e);
            }
        }

        private static AggregationSupplier load(final Class<?> inheritanceFrame, final Class<?> serviceType) throws ReflectiveOperationException {
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
        public AggregationSupplier load(final Class<?> serviceType) throws AggregationNotFoundException, ReflectiveOperationException {
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
            return this.<T>aggregate(objectType, () -> obj);
        }

        public <T> AggregationBuilder aggregate(final Class<T> objectType, final java.util.function.Supplier<T> factory){
            if (aggregator == null)
                aggregator = createAggregator();
            aggregator.providers.put(objectType, aggregator -> factory.get());
            return this;
        }

        @Override
        public AbstractAggregator get() {
            final AbstractAggregator result = this.aggregator;
            this.aggregator = null;
            return result == null ? createAggregator() : result;
        }

        public AbstractAggregator build(){
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
            if (e.getCause() instanceof AggregationNotFoundException)
                return queryObjectFallback(objectType, fallback);  //try fallback scenarios
            else throw new AggregationException(e.getCause());
        } catch (final ReflectiveOperationException e) {
            throw new AggregationException(e);
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

}
