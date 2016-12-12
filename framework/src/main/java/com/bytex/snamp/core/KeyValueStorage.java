package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Stateful;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents persistent storage for documents.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface KeyValueStorage {
    /**
     * Represents state of the record.
     */
    enum RecordState{
        /**
         * Record has no data.
         */
        EMPTY,
        /**
         * Record is modified in memory but not saved.
         */
        DIRTY,

        /**
         * Record is not changed.
         */
        UNCHANGED,

        /**
         * Record is deleted and no longer associated with persisted record.
         */
        DETACHED
    }

    /**
     * Represents entry stored in this storage.
     */
    interface Record extends Stateful{
        /**
         * Saves entry into storage.
         */
        void save();

        /**
         * All the fields are deleted but the record identity is maintained.
         */
        void clear();

        /**
         * Resets all changes.
         */
        void reset();

        /**
         * Gets version of this record.
         * @return Version of this record.
         */
        int getVersion();

        /**
         * Deletes this record.
         * @return {@literal true}, if this record is deleted successfully; {@literal false}, if record doesn't exist.
         */
        boolean delete();

        RecordState getState();
    }

    /**
     * Represents entry as key/value map.
     */
    interface MapRecordView extends Record{
        Map<String, ?> getAsMap();
        void setAsMap(final Map<String, ?> values);
    }

    /**
     * Represents entry as JSON.
     */
    interface JsonRecordView extends Record {
        Reader getAsJson();
        void setAsJson(final Reader value) throws IOException;
    }

    /**
     * Represents entry as plain text.
     */
    interface TextRecordView extends Record{
        String getAsText();
        void setAsText(final String value);
    }

    /**
     * Represents entry as {@code long} value.
     */
    interface LongRecordView extends Record{
        long getAsLong();
        void setAsLong(final long value);
    }

    /**
     * Represents entry as {@code double} value.
     */
    interface DoubleRecordView extends Record{
        double getAsDouble();
        void setAsDouble(final double value);
    }

    /**
     * Represents characteristics of this storage.
     */
    enum Characteristics{
        /**
         * The storage is persistent.
         */
        PERSISTENT,
        /**
         * The storage supports transactions with optimistic lock.
         */
        TRANSACTED
    }

    /**
     * Represents isolation level of transaction.
     */
    enum IsolationLevel {
        READ_COMMITTED,
        REPEATABLE_READ,
        READ_UNCOMMITTED
    }

    /**
     * Represents transaction scope.
     */
    interface TransactionScope extends SafeCloseable{
        /**
         * Gets isolation level of this transaction.
         * @return Isolation level.
         */
        IsolationLevel getIsolationLevel();

        /**
         * Commits all changes that was made during this transaction.
         */
        void commit();

        /**
         * Rollbacks all changes that was made during this transaction.
         */
        void rollback();

        /**
         * Unique ID of this transaction.
         * @return ID of this transaction.
         */
        String getId();
    }

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     */
    <R extends Record> R getRecord(final long key, final Class<R> recordView);

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     */
    <R extends Record> R getRecord(final double key, final Class<R> recordView);

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     */
    <R extends Record> R getRecord(final String key, final Class<R> recordView);

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     */
    <R extends Record> R getRecord(final Instant key, final Class<R> recordView);

    /**
     * Gets stream over all records in this storage.
     * @param recordType Type of the record view.
     * @param <R> Type of the record view.
     * @return Stream of records.
     */
    <R extends Record> Stream<R> getRecords(final Class<R> recordType);

    /**
     * Gets characteristics of this storage.
     * @return Characteristics of this storage.
     */
    Set<Characteristics> getCharacteristics();

    /**
     * Starts transaction.
     * @param level The required level of transaction.
     * @return A new transaction scope.
     * @throws UnsupportedOperationException Transactions are not supported by this storage.
     */
    TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException;
}
