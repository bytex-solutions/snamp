package com.bytex.snamp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Represents collection with a single element in it.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@NotThreadSafe
public final class SingletonMap<K, V> implements Map<K, V>, Map.Entry<K, V> {
    private final K key;
    private V value;

    public SingletonMap(@Nonnull final K k, @Nonnull final V v){
        key = k;
        value = v;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(@Nonnull final V v) {
        final V oldValue = value;
        value = v;
        return oldValue;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.key.equals(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.value.equals(value);
    }

    @Override
    public V get(@Nonnull final Object key) {
        return getOrDefault(key, null);
    }

    @Override
    public V put(@Nonnull final K key, @Nonnull final V value) {
        if(this.key.equals(key))
            return setValue(value);
        else
            throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSet<K> keySet() {
        return ImmutableSet.of(key);
    }

    @Override
    public ImmutableList<V> values() {
        return ImmutableList.of(value);
    }

    @Override
    public ImmutableSet<Entry<K, V>> entrySet() {
        return ImmutableSet.of(this);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return this.key.equals(key) ? value : defaultValue;
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        action.accept(key, value);
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        value = function.apply(key, value);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        if(this.key.equals(key))
            return value;
        else
            throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final boolean success;
        if(success = this.key.equals(key) && value.equals(oldValue))
            value = newValue;
        return success;
    }

    @Override
    public V replace(final K key, final V newValue) {
        return this.key.equals(key) ? setValue(newValue) : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    private boolean equals(final Map<?, ?> other) {
        return size() == other.size() &&
                other.containsKey(key) &&
                other.containsValue(value);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Map<?, ?> && equals((Map<?, ?>) other);
    }

    @Override
    public String toString() {
        return "SingletonMap{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
