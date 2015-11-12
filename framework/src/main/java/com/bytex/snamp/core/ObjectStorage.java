package com.bytex.snamp.core;

import java.util.concurrent.ConcurrentMap;

/**
 * Provides access to cluster-wide storage.
 */
public interface ObjectStorage {
    /**
     * Gets cluster-wide collection.
     * @param collectionName Name of the cluster-wide collection.
     * @return Instance of the cluster-wide collection.
     */
    ConcurrentMap<String, Object> getCollection(final String collectionName);

    /**
     * Deletes the specified collection.
     * @param collectionName Name of the collection to remove.
     */
    void deleteCollection(final String collectionName);
}
