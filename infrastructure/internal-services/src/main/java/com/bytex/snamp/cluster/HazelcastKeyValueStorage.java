package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.core.KeyValueStorage;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents non-persistent high-performance key/value storage backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastKeyValueStorage extends HazelcastSharedObject implements KeyValueStorage {
    private final IMap<String, Serializable> storage;

    HazelcastKeyValueStorage(final HazelcastInstance hazelcast, final String name){
        storage = hazelcast.getMap(name);
    }

    /**
     * Gets name of the distributed service.
     *
     * @return Name of this distributed service.
     */
    @Override
    public String getName() {
        return storage.getName();
    }

    /**
     * Gets record associated with the specified key.
     *
     * @param key        The key of the record. Cannot be {@literal null}.
     * @param recordView Type of the record representation.
     * @return Selector for records in this storage.
     * @throws ClassCastException Unsupported record view.
     */
    @Override
    public <R extends Record> Optional<R> getRecord(final Comparable<?> key, final Class<R> recordView) {
        return null;
    }

    /**
     * Gets record associated with the specified key.
     *
     * @param key         The key of the record.
     * @param recordView  Type of the record representation.
     * @param initializer A function used to initialize record for the first time when it is created.
     * @return Existing or newly created record.
     * @throws E Unable to initialize record.
     */
    @Override
    public <R extends Record, E extends Throwable> R getOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> initializer) throws E {
        return null;
    }

    /**
     * Deletes the record associated with key.
     *
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    @Override
    public boolean delete(final Comparable<?> key) {
        return false;
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return false;
    }

    /**
     * Gets stream over all records in this storage.
     *
     * @param recordType Type of the record view.
     * @return Stream of records.
     */
    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        return null;
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        storage.clear();
    }

    /**
     * Determines whether this storage supports transactions.
     *
     * @return {@literal true} if transactions are supported; otherwise, {@literal false}.
     */
    @Override
    public boolean isTransactional() {
        return false;
    }


    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Transaction is not supported");
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return false;
    }
}
