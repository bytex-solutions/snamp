package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Stateful;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents persistent storage for documents.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface KeyValueStorage extends Closeable, SharedObject {
    /**
     * Represents state of the record.
     */
    enum RecordState{
        /**
         * Record has no data.
         */
        NEW,

        /**
         * Record is active.
         */
        ACTIVE,

        /**
         * Record is deleted and no longer associated with persisted record.
         */
        DETACHED
    }

    /**
     * Represents entry stored in this storage.
     */
    @NotThreadSafe
    interface Record{
        /**
         * Synchronize record stored at server with local version of this record.
         */
        void refresh();

        /**
         * Gets version of this record.
         * @return Version of this record.
         */
        int getVersion();

        /**
         * Gets state of this record.
         * @return State of this record.
         */
        RecordState getState();
    }

    /**
     * Represents entry as serializable single value.
     */
    interface SerializableRecordView extends Record{
        Serializable getValue();
        void setValue(final Serializable value);
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
     * Represents isolation level of transaction.
     */
    enum IsolationLevel {
        READ_COMMITTED,
        REPEATABLE_READ
    }

    /**
     * Represents transaction scope.
     */
    interface TransactionScope extends SafeCloseable{

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
        int getId();
    }

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     * @throws ClassCastException Unsupported record view.
     */
    <R extends Record> R getRecord(final Comparable<?> key, final Class<R> recordView);

    /**
     * Deletes the record associated with key.
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    boolean delete(final Comparable<?> key);

    /**
     * Determines whether the record of the specified key exists.
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    boolean exists(final Comparable<?> key);

    /**
     * Gets stream over all records in this storage.
     * @param recordType Type of the record view.
     * @param <R> Type of the record view.
     * @return Stream of records.
     */
    <R extends Record> Stream<R> getRecords(final Class<R> recordType);

    /**
     * Removes all record.
     */
    void clear();

    /**
     * Determines whether this storage supports transactions.
     * @return {@literal true} if transactions are supported; otherwise, {@literal false}.
     */
    boolean isTransactional();

    /**
     * Starts transaction.
     * @param level The required level of transaction.
     * @return A new transaction scope.
     * @throws UnsupportedOperationException Transactions are not supported by this storage.
     */
    TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException;

    boolean isViewSupported(final Class<? extends Record> recordView);
}
