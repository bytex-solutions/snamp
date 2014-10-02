package com.itworks.snamp;

import com.itworks.snamp.internal.annotations.ThreadSafe;

import java.util.*;

import static java.util.Map.Entry;

/**
 * Represents map builder. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public final class MapBuilder<K, V> implements Iterable<Entry<K, V>> {
    /**
     * Represents empty map factory.
     * @param <M> Type of the produced map.
     * @param <K> Type of the keys in the map.
     * @param <V> Type of the values in the map.
     * @author Roman Sakno
     * @since 1.0
     */
    public static interface MapFactory<M extends Map<K, V>, K, V>{
        /**
         * Creates a new instance of the empty map.
         * @param size The size of the empty map. Can be used as capacity.
         * @return A new instance of the empty map.
         */
        M createMap(final int size);
    }

    private static final class KeyValueNode<K, V> implements Entry<K, V>{
        private KeyValueNode<K, V> next;
        private final K key;
        private final V value;

        public KeyValueNode(final K key, final V value){
            this.key = key;
            this.value = value;
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
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }

        private KeyValueNode<K, V> attach(final K key, final V value){
            return next = new KeyValueNode<>(key, value);
        }

        private KeyValueNode<K, V> attach(final Entry<K, V> entry){
            return attach(entry.getKey(), entry.getValue());
        }
    }

    private KeyValueNode<K, V> first;
    private KeyValueNode<K, V> last;
    private int size;

    private MapBuilder(){
        size = 0;
        first = last = null;
    }

    private MapBuilder(final K key, final V value){
        size = 1;
        first = last = new KeyValueNode<>(key, value);
    }

    /**
     * Initializes a new map builder with empty elements.
     * @param <K> The definition of the key type.
     * @param <V> The definitionof the value type.
     * @return A new map builder.
     */
    public static <K, V> MapBuilder<K, V> create(){
        return new MapBuilder<>();
    }

    /**
     * Initializes a new map builder with initial key/value pair.
     * @param key
     * @param value
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> MapBuilder<K, V> create(final K key, final V value){
        return new MapBuilder<>(key, value);
    }

    public MapBuilder<K, V> add(final K key, final V value) {
        if(first == null)
            first = last = new KeyValueNode<>(key, value);
        else last = last.attach(key, value);
        size += 1;
        return this;
    }

    @SafeVarargs
    public final MapBuilder<K, V> add(final Entry<K, V>... values) {
        for (final Entry<K, V> pair : values)
            add(pair.getKey(), pair.getValue());
        return this;
    }

    private void fill(final Map<K, V> output) {
        KeyValueNode<K, V> current = first;
        while (current != null) {
            output.put(current.getKey(), current.getValue());
            current = current.next;
        }
    }

    public <M extends Map<K, V>> M buildMap(final MapFactory<M, K, V> mapFactory) {
        final M result = mapFactory.createMap(size);
        fill(result);
        return result;
    }

    public HashMap<K, V> buildHashMap() {
        return buildMap(new MapFactory<HashMap<K, V>, K, V>() {
            @Override
            public HashMap<K, V> createMap(final int size) {
                return new HashMap<>(size);
            }
        });
    }

    public Map<K, V> buildUnmodifiableMap() {
        return Collections.unmodifiableMap(buildHashMap());
    }

    public LinkedHashMap<K, V> buildLinkedHashMap() {
        return buildMap(new MapFactory<LinkedHashMap<K, V>, K, V>() {
            @Override
            public LinkedHashMap<K, V> createMap(final int size) {
                return new LinkedHashMap<K, V>(size);
            }
        });
    }

    public TreeMap<K, V> buildTreeMap() {
        return buildMap(new MapFactory<TreeMap<K, V>, K, V>() {
            @Override
            public TreeMap<K, V> createMap(final int size) {
                return new TreeMap<>();
            }
        });
    }

    /**
     * Returns an iterator over a set of key/value pairs.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new Iterator<Entry<K, V>>() {
            private final KeyValueNode<K, V> first = MapBuilder.this.first;
            private KeyValueNode<K, V> current = null;

            @Override
            public boolean hasNext() {
                return current == null ? first != null : current.next != null;
            }

            @Override
            public Entry<K, V> next() {
                return current = current == null ? first : current.next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
