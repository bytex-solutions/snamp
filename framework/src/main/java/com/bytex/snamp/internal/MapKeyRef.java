package com.bytex.snamp.internal;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

/**
 * Represents weak reference to the record in the map.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class MapKeyRef<K, V> extends WeakReference<Map<K, V>> implements Map.Entry<K, V> {
    private final K key;

    public MapKeyRef(final Map<K, V> map, final K key){
        super(map);
        this.key = Objects.requireNonNull(key);
    }

    /**
     * Returns the key corresponding to this entry.
     *
     * @return the key corresponding to this entry
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map.
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * Returns the value corresponding to this entry.  If the mapping
     * has been removed from the backing map (by the iterator's
     * <tt>remove</tt> operation), the results of this call are undefined.
     *
     * @return the value corresponding to this entry
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map.
     */
    @Override
    public V getValue() {
        final Map<K, V> map = get();
        if(map == null)
            throw new IllegalStateException("Underlying map is collected by GC");
        else return map.get(key);
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  (Writes through to the map.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the map (by the iterator's <tt>remove</tt> operation).
     *
     * @param value new value to be stored in this entry
     * @return old value corresponding to the entry
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by the backing map
     * @throws ClassCastException            if the class of the specified value
     *                                       prevents it from being stored in the backing map
     * @throws NullPointerException          if the backing map does not permit
     *                                       null values, and the specified value is null
     * @throws IllegalArgumentException      if some property of this value
     *                                       prevents it from being stored in the backing map
     * @throws IllegalStateException         implementations may, but are not
     *                                       required to, throw this exception if the entry has been
     *                                       removed from the backing map.
     */
    @Override
    public V setValue(final V value) {
        final Map<K, V> map = get();
        if(map == null)
            throw new IllegalStateException("Underlying map is collected by GC");
        else return map.put(key, value);
    }
}
