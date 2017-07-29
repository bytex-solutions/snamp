package com.bytex.snamp.gateway;

import com.bytex.snamp.EntryReader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Objects;

/**
 * This class emulates {@link com.google.common.collect.Multimap} with weak values.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class WeakMultimap {

    private WeakMultimap(){
        throw new InstantiationError();
    }

    static <K, V> boolean put(final Multimap<K, WeakReference<V>> map,
                                    final K key,
                                    final V value){
        return map.put(key, new WeakReference<>(value));
    }

    static <K, V> int remove(final Multimap<K, WeakReference<V>> map,
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
     * Iterates through entries in the multimap and, additionally, removes dead references.
     * @param map The map to update.
     * @param <K> Type of the keys in map.
     * @param <V> Type of the values in map.
     * @param reader Entry reader.
     */
    static <K, V, E extends Exception> void iterate(final Multimap<K, WeakReference<V>> map, final EntryReader<K, V, E> reader) throws E {
        final ImmutableSet<K> keys = ImmutableSet.copyOf(map.keySet());
        for (final K key : keys)
            for (final Iterator<WeakReference<V>> it = map.get(key).iterator(); it.hasNext(); ) {
                final WeakReference<V> ref = it.next();
                final V value = ref != null ? ref.get() : null;
                if (ref == null || value == null)
                    it.remove();
                else if (!reader.accept(key, value)) break;
            }
    }
}
