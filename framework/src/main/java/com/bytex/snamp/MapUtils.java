package com.bytex.snamp;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.function.*;

/**
 * Represents utility methods for working with {@link java.util.Map} and {@link java.util.Dictionary}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MapUtils {
    private MapUtils(){
        throw new InstantiationError();
    }

    public static <K, V> int getValueAsInt(final Map<K, V> map, final K key, final ToIntFunction<? super V> transform, final IntSupplier defaultValue) {
        return map.containsKey(key) ? transform.applyAsInt(map.get(key)) : defaultValue.getAsInt();
    }

    public static <K, V> long getValueAsLong(final Map<K, V> map, final K key, final ToLongFunction<? super V> transform, final LongSupplier defaultValue){
        return map.containsKey(key) ? transform.applyAsLong(map.get(key)) : defaultValue.getAsLong();
    }

    public static <K, V> double getValueAsDouble(final Map<K, V> map, final K key, final ToDoubleFunction<? super V> transform, final DoubleSupplier defaultValue){
        return map.containsKey(key) ? transform.applyAsDouble(map.get(key)) : defaultValue.getAsDouble();
    }

    private static <K, V, O> O getValueConditionally(final Predicate<? super K> keyExistence,
                                        final Function<? super K, ? extends V> keyResolver,
                                        final K key,
                                        final Predicate<? super V> valueCondition,
                                        final Function<? super V, ? extends O> transform,
                                        final Supplier<? extends O> defaultValue) {
        if(keyExistence.test(key)){
            final V value = keyResolver.apply(key);
            if(valueCondition.test(value))
                return transform.apply(value);
        }
        return defaultValue.get();
    }

    public static <K, V, O> O getValue(final Map<K, V> map, final K key, final Function<? super V, ? extends O> transform, final Supplier<? extends O> defaultValue) {
        return getValueConditionally(map::containsKey, map::get, key, value -> true, transform, defaultValue);
    }

    public static <K, V, O> O getValue(final Map<K, V> map, final K key, final Class<O> expectedType, final Supplier<? extends O> defaultValue){
        return getValueConditionally(map::containsKey, map::get, key, expectedType::isInstance, expectedType::cast, defaultValue);
    }

    public static <K, V, O> O getValue(final Dictionary<K, V> map, final K key, final Class<O> expectedType, final Supplier<? extends O> defaultValue) {
        return getValueConditionally(k -> map.get(k) != null, map::get, key, expectedType::isInstance, expectedType::cast, defaultValue);
    }

    public static <K, V, O> O getValue(final Dictionary<K, V> map, final K key, final Function<? super V, ? extends O> transform, final Supplier<? extends O> defaultValue) {
        return getValueConditionally(k -> map.get(k) != null, map::get, key, value -> true, transform, defaultValue);
    }

    public static <K, V, O, E extends Throwable> O getIfPresent(final Map<K, V> map, final K key, final Function<? super V, ? extends O> transform, final Function<? super K, ? extends E> exceptionFactory) throws E{
        if(map.containsKey(key))
            return transform.apply(map.get(key));
        else
            throw exceptionFactory.apply(key);
    }

    public static <K, V, O> boolean acceptIfPresent(final Map<K, V> map, final K key, final Function<? super V, ? extends O> transform, final Consumer<? super O> acceptor) {
        final boolean exists;
        if (exists = map.containsKey(key))
            acceptor.accept(transform.apply(map.get(key)));
        return exists;
    }

    public static <K, V> boolean acceptIntIfPresent(final Map<K, V> map, final K key, final ToIntFunction<? super V> transform, final IntConsumer acceptor) {
        final boolean exists;
        if (exists = map.containsKey(key))
            acceptor.accept(transform.applyAsInt(map.get(key)));
        return exists;
    }

    private static <K, I, V> V putValue(final BiFunction<K, V, V> putter,
                                        final K key,
                                        final I value,
                                        final Function<? super I, ? extends V> transform){
        return putter.apply(key, transform.apply(value));
    }

    public static <K, I, V> V putValue(final Map<K, V> map, final K key, final I value, final Function<? super I, ? extends V> transform){
        return putValue(map::put, key, value, transform);
    }

    public static <K, I, V> V putValue(final Dictionary<K, V> map, final K key, final I value, final Function<? super I, ? extends V> transform){
        return putValue(map::put, key, value, transform);
    }

    public static <K, V> V putIntValue(final Map<K, V> map, final K key, final int value, final IntFunction<? extends V> transform){
        return map.put(key, transform.apply(value));
    }

    public static Properties toProperties(final Map<String, String> params){
        final Properties props = new Properties();
        props.putAll(params);
        return props;
    }
}
