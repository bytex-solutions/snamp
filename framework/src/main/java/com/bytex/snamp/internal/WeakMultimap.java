package com.bytex.snamp.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Objects;

/**
 * This class emulates {@link com.google.common.collect.Multimap} with weak values.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WeakMultimap {

    private WeakMultimap(){

    }

    public static <K, V> boolean put(final Multimap<K, WeakReference<V>> map,
                                    final K key,
                                    final V value){
        return map.put(key, new WeakReference<>(value));
    }

    public static <K, V> int remove(final Multimap<K, WeakReference<V>> map,
                                        final K key,
                                        final V value){
        int result = 0;
        for(final Iterator<WeakReference<V>> it = map.get(key).iterator(); it.hasNext();) {
            final V candidate = it.next().get();
            if(candidate == null)
                it.remove();
            else if (Objects.equals(value, candidate)) {
                it.remove();
                result += 1;
            }
        }
        return result;
    }

    /**
     * Removes dead references from the map.
     * @param map The map to update.
     * @param <K> Type of the keys in map.
     * @param <V> Type of the values in map.
     */
    public static <K, V> void gc(final Multimap<K, WeakReference<V>> map){
        final ImmutableSet<K> keys = ImmutableSet.copyOf(map.keySet());
        for(final K key: keys)
            for(final Iterator<WeakReference<V>> it = map.get(key).iterator(); it.hasNext();){
                final WeakReference<V> ref = it.next();
                if(ref == null || ref.get() == null)
                    it.remove();
            }
    }
}
