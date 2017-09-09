package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.SafeCloseable;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.*;

/**
 * Represents persistent storage for documents.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface KeyValueStorage extends SharedObject {
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
         * Determines whether this record is no longer associated with storage.
         * @return {@literal true}, if this record is detached from the storage; otherwise, {@literal false}.
         */
        boolean isDetached();
    }

    /**
     * Represents entry as serializable single value.
     */
    interface SerializableRecordView extends Record{
        /**
         * Gets serializable value stored in the view.
         * @return Sets serializable value stored in the view.
         */
        Serializable getValue();
        void setValue(final Serializable value);
    }

    /**
     * Represents entry as key/value map.
     */
    interface MapRecordView extends Record{
        Acceptor<MapRecordView, ExceptionPlaceholder> INITIALIZER = record -> record.setAsMap(ImmutableMap.of());
        Map<String, ?> getAsMap();
        void setAsMap(final Map<String, ?> values);
    }

    /**
     * Represents entry as JSON document.
     */
    interface JsonRecordView extends Record {
        Acceptor<JsonRecordView, IOException> INITIALIZER = record -> {
            try(final StringReader reader = new StringReader("{}")){
                record.setAsJson(reader);
            }
        };
        Reader getAsJson();
        void setAsJson(final Reader value) throws IOException;
        Writer createJsonWriter();
    }

    /**
     * Represents entry as plain text.
     */
    interface TextRecordView extends Record{
        Acceptor<TextRecordView, ExceptionPlaceholder> INITIALIZER = record -> record.setAsText("");
        String getAsText();
        void setAsText(final String value);
    }

    /**
     * Represents entry as {@code long} value.
     */
    interface LongRecordView extends Record, LongSupplier, LongConsumer{
        Acceptor<LongRecordView, ExceptionPlaceholder> INITIALIZER = record -> record.accept(0L);
        long getAsLong();
        void accept(final long value);
    }

    /**
     * Represents entry as {@code double} value.
     */
    interface DoubleRecordView extends Record, DoubleSupplier, DoubleConsumer{
        Acceptor<DoubleRecordView, ExceptionPlaceholder> INITIALIZER = record -> record.accept(0D);
        double getAsDouble();
        void accept(final double value);
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
     * <p>
     *     List of supported key types:
     *     <ul>
     *         <li>{@link java.util.UUID}</li>
     *         <li>{@link String}</li>
     *         <li>{@link Byte}</li>
     *         <li>{@link Short}</li>
     *         <li>{@link Integer}</li>
     *         <li>{@link Long}</li>
     *         <li>{@link java.math.BigInteger}</li>
     *         <li>{@link java.math.BigDecimal}</li>
     *         <li>{@link Character}</li>
     *         <li>{@link Double}</li>
     *         <li>{@link Float}</li>
     *         <li>{@link java.util.Date}</li>
     *         <li>{@link java.time.Instant}</li>
     *     </ul>
     * @param key The key of the record. Cannot be {@literal null}.
     * @param recordView Type of the record representation.
     * @param <R> Type of the record view.
     * @return Selector for records in this storage.
     * @throws ClassCastException Unsupported record view.
     */
    <R extends Record> Optional<R> getRecord(final Comparable<?> key, final Class<R> recordView);

    /**
     * Gets record associated with the specified key.
     * @param key The key of the record.
     * @param recordView Type of the record representation.
     * @param initializer A function used to initialize record for the first time when it is created.
     * @param <R> Type of record to get or create.
     * @param <E> Type of exception that can be produced by initializer.
     * @throws E Unable to initialize record.
     * @return Existing or newly created record.
     */
    <R extends Record, E extends Throwable> R getOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> initializer) throws E;

    /**
     * Updates or creates record associated with the specified key.
     * @param key The key of the record.
     * @param recordView Type of the record representation.
     * @param updater Record updater.
     * @param <R> Type of record to get or create.
     * @param <E> Type of exception that can be produced by updater.
     * @throws E Unable to update record.
     */
    <R extends Record, E extends Throwable> void updateOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> updater) throws E;

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
     * Iterates over records.
     * @param recordType Type of the record representation.
     * @param filter Query filter. Cannot be {@literal null}.
     * @param reader Record reader. Cannot be {@literal null}.
     * @param <R> Record representation.
     * @param <E> Exception that can be thrown by reader.
     * @throws E Reading failed.
     */
    <R extends Record, E extends Throwable> void forEachRecord(final Class<R> recordType,
                                                               final Predicate<? super Comparable<?>> filter,
                                                               final EntryReader<? super Comparable<?>, ? super R, E> reader) throws E;

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
     * Determines whether this storage is persistent.
     * @return {@literal true}, if this storage is persistent; otherwise, {@literal false}.
     * @since 2.1
     */
    boolean isPersistent();

    /**
     * Gets all keys in this storage.
     * @return All keys in this storage.
     */
    Set<? extends Comparable<?>> keySet();

    /**
     * Starts transaction.
     * @param level The required level of transaction.
     * @return A new transaction scope.
     * @throws UnsupportedOperationException Transactions are not supported by this storage; or isolation level is not supported.
     */
    TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException;

    boolean isViewSupported(final Class<? extends Record> recordView);
}
