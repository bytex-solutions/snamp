package com.bytex.snamp.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Represents immutable empty map.
 * @param <K> Type of keys in the map.
 * @param <V> Type of values in the map.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ImmutableEmptyMap<K, V> implements Map<K, V> {
    @Override
    public final int size() {
        return 0;
    }

    @Override
    public final boolean isEmpty() {
        return true;
    }

    @Override
    public final boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public final boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public final V get(final Object key) {
        return null;
    }

    @Override
    public final V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nonnull
    public final Set<K> keySet() {
        return ImmutableSet.of();
    }

    @Override
    @Nonnull
    public final Collection<V> values() {
        return ImmutableList.of();
    }

    @Override
    @Nonnull
    public final Set<Map.Entry<K, V>> entrySet() {
        return ImmutableSet.of();
    }

    @Override
    public final V getOrDefault(final Object key, final V defaultValue) {
        return defaultValue;
    }

    @Override
    public final void forEach(final BiConsumer<? super K, ? super V> action) {

    }

    @Override
    public final void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }

    @Override
    public boolean equals(final Object obj){
        return getClass().isInstance(obj);
    }
}
