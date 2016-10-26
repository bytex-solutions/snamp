package com.bytex.snamp.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class HazelcastStorage implements ConcurrentMap<String, Object> {
    private final IMap<String, Object> underlyingMap;

    HazelcastStorage(final HazelcastInstance hazelcast, final String collectionName){
        underlyingMap = hazelcast.getMap(collectionName);
    }

    @Override
    public Object putIfAbsent(final String key, final Object value) {
        return underlyingMap.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return underlyingMap.remove(key, value);
    }

    @Override
    public boolean replace(final String key, final Object oldValue, final Object newValue) {
        return underlyingMap.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(final String key, final Object value) {
        return underlyingMap.replace(key, value);
    }

    @Override
    public int size() {
        return underlyingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return underlyingMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return underlyingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return underlyingMap.containsValue(value);
    }

    @Override
    public Object get(final Object key) {
        return underlyingMap.get(key);
    }

    @Override
    public Object put(final String key, final Object value) {
        return underlyingMap.put(key, value);
    }

    @Override
    public Object remove(final Object key) {
        return underlyingMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ?> m) {
        underlyingMap.putAll(m);
    }

    @Override
    public void clear() {
        underlyingMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return underlyingMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return underlyingMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return underlyingMap.entrySet();
    }

    @Override
    public String toString(){
        return underlyingMap.toString();
    }
}
