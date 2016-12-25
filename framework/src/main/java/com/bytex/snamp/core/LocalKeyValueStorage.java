package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents in-memory implementation of KV storage.
 * @since 2.0
 * @version 2.0
 */
final class LocalKeyValueStorage extends ConcurrentHashMap<Comparable<?>, InMemoryRecord> implements KeyValueStorage {
    private static final long serialVersionUID = 4077365936278946842L;

    private final String name;

    LocalKeyValueStorage(final String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPersistent() {
        return false;
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
        final Record result = get(key);
        return result == null ? Optional.empty() : Optional.of(result).map(recordView::cast);
    }

    /**
     * Gets record associated with the specified key.
     *
     * @param key         The key of the record.
     * @param recordView  Type of the record representation.
     * @param initializer A function used to initialize record for the first time when it is created.
     * @return Existing or newly created record.
     */
    @Override
    public <R extends Record, E extends Throwable> R getOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> initializer) throws E {
        InMemoryRecord result = get(key);
        if (result == null) {
            final InMemoryRecord newRecord = new InMemoryRecord();
            newRecord.init(initializer, recordView);
            result = firstNonNull(putIfAbsent(key, newRecord), newRecord);
        }
        return recordView.cast(result);
    }

    /**
     * Updates or creates record associated with the specified key.
     *
     * @param key        The key of the record.
     * @param recordView Type of the record representation.
     * @param updater    Record updater.
     * @throws E Unable to update record.
     */
    @Override
    public <R extends Record, E extends Throwable> void updateOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> updater) throws E {
        InMemoryRecord result = get(key);
        if (result == null) {
            final InMemoryRecord newRecord = new InMemoryRecord();
            result = firstNonNull(putIfAbsent(key, newRecord), newRecord);
        }
        updater.accept(recordView.cast(result));
    }

    @Override
    public boolean delete(final Comparable<?> key) {
        final InMemoryRecord record = remove(key);
        final boolean success;
        if (success = record != null)
            record.detach();
        return success;
    }

    @Override
    public boolean exists(final Comparable<?> key) {
        return containsKey(key);
    }

    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        return values().stream().map(recordType::cast);
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return recordView.isAssignableFrom(InMemoryRecord.class);
    }
}
