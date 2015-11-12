package com.bytex.snamp.core;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents local storage in non-clustered environment.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class InMemoryObjectStorage implements ObjectStorage {
    private final ConcurrentMap<String, ConcurrentMap<String, Object>> collections;

    InMemoryObjectStorage(){
        collections = Maps.newConcurrentMap();
    }

    private synchronized ConcurrentMap<String, Object> getCollectionSync(final String collectionName){
        final ConcurrentMap<String, Object> result;
        if(collections.containsKey(collectionName))
            result = collections.get(collectionName);
        else collections.put(collectionName, result = Maps.newConcurrentMap());
        return result;
    }

    @Override
    public ConcurrentMap<String, Object> getCollection(final String collectionName) {
        return collections.containsKey(collectionName) ? collections.get(collectionName) : getCollectionSync(collectionName);
    }

    /**
     * Deletes the specified collection.
     *
     * @param collectionName Name of the collection to remove.
     */
    @Override
    public void deleteCollection(final String collectionName) {
        final Map<String, Object> collection;
        synchronized (this) {
            collection = collections.remove(collectionName);
        }
        if (collection != null) collection.clear();
    }
}
