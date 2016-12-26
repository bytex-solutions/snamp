package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents in-memory implementation of KV storage.
 * @since 2.0
 * @version 2.0
 */
final class InMemoryKeyValueStorage extends ConcurrentHashMap<Comparable<?>, InMemoryRecord> implements KeyValueStorage {
    private static final long serialVersionUID = 4077365936278946842L;

    private final String name;

    InMemoryKeyValueStorage(final String name){
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


    /**
     * Iterates over records.
     *
     * @param recordType Type of the record representation.
     * @param filter     Query filter. Cannot be {@literal null}.
     * @param reader     Record reader. Cannot be {@literal null}.
     * @throws E Reading failed.
     */
    @Override
    public <R extends Record, E extends Throwable> void forEachRecord(final Class<R> recordType, final Predicate<? super Comparable<?>> filter, final EntryReader<? super Comparable<?>, ? super R, E> reader) throws E {
        for (final Entry<Comparable<?>, InMemoryRecord> entry : entrySet())
            if (filter.test(entry.getKey()))
                if (!reader.accept(entry.getKey(), recordType.cast(entry.getValue())))
                    return;
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
