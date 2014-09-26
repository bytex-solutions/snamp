package com.itworks.snamp.internal;

import com.itworks.snamp.internal.annotations.Internal;

import java.util.*;

/**
 * Represents map builder. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public final class MapBuilder<K, V> {
    private final Map<K, V> m;

    private MapBuilder(final int initialCapacity){
        m = new LinkedHashMap<>(initialCapacity);
    }

    public static <K, V> MapBuilder<K, V> create(){
        return new MapBuilder<>(3);
    }


    public static <K, V> MapBuilder<K, V> create(final int initialCapacity){
        return new MapBuilder<>(initialCapacity);
    }

    public static <K, V> MapBuilder<K, V> create(final K key, final V value){
        return MapBuilder.<K, V>create().add(key, value);
    }

    public static <K, V> MapBuilder<K, V> create(final K key, final V value, final int initialCapacity){
        return MapBuilder.<K, V>create(initialCapacity).add(key, value);
    }

    public MapBuilder<K, V> add(final K key, final V value) {
        m.put(key, value);
        return this;
    }

    public MapBuilder<K, V> add(final Map.Entry<K, V>... values){
        for(final Map.Entry<K, V> pair: values)
            m.put(pair.getKey(), pair.getValue());
        return this;
    }

    public HashMap<K, V> buildHashMap() {
        return new HashMap<>(m);
    }

    public Map<K, V> buildUnmodifiableMap() {
        return Collections.unmodifiableMap(m);
    }

    public LinkedHashMap<K, V> buildLinkedHashMap() {
        return new LinkedHashMap<>(m);
    }

    public TreeMap<K, V> buildTreeMap() {
        return new TreeMap<>(m);
    }

    public Map<K, V> getMap(){
        return m;
    }
}
