package com.bytex.snamp;

import com.google.common.collect.ImmutableSet;

import java.util.*;
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

    public static <K, V> OptionalInt getValueAsInt(final Map<K, V> map, final K key, final ToIntFunction<? super V> transform) {
        return map.containsKey(key) ? OptionalInt.of(transform.applyAsInt(map.get(key))) : OptionalInt.empty();
    }

    public static <K, V> OptionalLong getValueAsLong(final Map<K, V> map, final K key, final ToLongFunction<? super V> transform) {
        return map.containsKey(key) ? OptionalLong.of(transform.applyAsLong(map.get(key))) : OptionalLong.empty();
    }

    public static <K, V> OptionalDouble getValueAsDouble(final Map<K, V> map, final K key, final ToDoubleFunction<? super V> transform) {
        return map.containsKey(key) ? OptionalDouble.of(transform.applyAsDouble(map.get(key))) : OptionalDouble.empty();
    }

    private static <K, V, O> Optional<O> getValueConditionally(final Predicate<? super K> keyExistence,
                                        final Function<? super K, ? extends V> keyResolver,
                                        final K key,
                                        final Predicate<? super V> valueCondition,
                                        final Function<? super V, ? extends O> transform) {
        if(keyExistence.test(key)){
            final V value = keyResolver.apply(key);
            if(valueCondition.test(value))
                return Optional.of(value).map(transform);
        }
        return Optional.empty();
    }

    public static <K, V, O> Optional<O> getValue(final Map<K, V> map, final K key, final Function<? super V, ? extends O> transform) {
        return getValueConditionally(map::containsKey, map::get, key, value -> true, transform);
    }

    public static <K, V, O> Optional<O> getValue(final Map<K, V> map, final K key, final Class<O> expectedType) {
        return getValue(map, key, expectedType::cast);
    }

    public static <K, V, O> Optional<O> getValue(final Dictionary<K, V> map, final K key, final Function<? super V, O> transform) {
        return getValueConditionally(k -> map.get(k) != null, map::get, key, value -> true, transform);
    }

    public static <K, V, O> Optional<O> getValue(final Dictionary<K, V> map, final K key, final Class<O> expectedType) {
        return getValue(map, key, expectedType::cast);
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

    public static <K, V> V putIntValue(final Map<K, V> map, final K key, final int value, final IntFunction<? extends V> transform){
        return map.put(key, transform.apply(value));
    }

    public static Properties toProperties(final Map<String, String> params){
        final Properties props = new Properties();
        props.putAll(params);
        return props;
    }

    public static <K, V> Map<K, V> readOnlyMap(final Function<? super K, ? extends V> keyGetter,
                                                        final K... keys){
        return new FixedKeysMap<>(keyGetter, null, ImmutableSet.copyOf(keys));
    }

    public static <K, V> Map<K, V> readOnlyMap(final Function<? super K, ? extends V> keyGetter,
                                                        final Collection<? extends K> keys){
        return new FixedKeysMap<>(keyGetter, null, ImmutableSet.copyOf(keys));
    }

    public static <K, V> Map<K, V> readWriteMap(final Function<? super K, ? extends V> keyGetter,
                                                         final BiFunction<? super K, ? super V, ? extends V> keySetter,
                                                         final K... keys){
        return new FixedKeysMap<>(keyGetter, Objects.requireNonNull(keySetter), ImmutableSet.copyOf(keys));
    }

    public static <K, V> Map<K, V> readWriteMap(final Function<? super K, ? extends V> keyGetter,
                                                         final BiFunction<? super K, ? super V, ? extends V> keySetter,
                                                         final Collection<? extends K> keys){
        return new FixedKeysMap<>(keyGetter, Objects.requireNonNull(keySetter), ImmutableSet.copyOf(keys));
    }

    public static <K, V> boolean putIf(final Map<K, ? super V> map, final K key, final V value, final Predicate<? super V> condition) {
        final boolean success;
        if (success = condition.test(value))
            map.put(key, value);
        return success;
    }
}
