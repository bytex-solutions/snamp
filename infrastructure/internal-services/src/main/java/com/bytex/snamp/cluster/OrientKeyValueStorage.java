package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.KeyValueStorage;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexKeyCursor;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bytex.snamp.cluster.DBUtils.withDatabase;

/**
 * Represents key/value storage backed by OrientDB.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class OrientKeyValueStorage extends GridSharedObject implements KeyValueStorage {
    private static final class TransactionScopeImpl extends OTransactionOptimistic implements TransactionScope {
        private TransactionScopeImpl(final ODatabaseDocumentTx iDatabase) {
            super(iDatabase);
        }
    }

    private final OClass documentClass;
    private final ODatabaseDocumentTx database;
    private final String indexName;

    OrientKeyValueStorage(final ODatabaseDocumentTx database,
                          final String collectionName) {
        super(collectionName);
        indexName = collectionName + "Index";
        try (final SafeCloseable ignored = withDatabase(this.database = database)) {
            //init class
            final OSchema schema = database.getMetadata().getSchema();
            if (schema.existsClass(collectionName))
                documentClass = schema.getClass(collectionName);
            else {
                documentClass = schema.createClass(collectionName);
                PersistentFieldDefinition.defineFields(documentClass);
                PersistentFieldDefinition.createIndex(documentClass, indexName);
            }
        }
    }

    private <I, V> Optional<V> getRecord(final Comparable<?> indexKey, final Function<? super OIdentifiable, ? extends V> transform) {
        final OIdentifiable recordId;
        try (final SafeCloseable ignored = withDatabase(database)) {
            recordId = (OIdentifiable) documentClass.getClassIndex(indexName).get(PersistentFieldDefinition.getCompositeKey(indexKey));
        }
        return Optional.ofNullable(recordId).map(transform);
    }

    @Override
    public boolean isPersistent() {
        return true;
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
        final Optional<PersistentRecord> record = getRecord(key, PersistentRecord::new);
        record.ifPresent(rec -> rec.setDatabase(database).setClassName(documentClass.getName()));
        return record.map(database::load).map(recordView::cast);
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
        PersistentRecord record = getRecord(key, PersistentRecord::new).orElse(null);
        final boolean isNew;
        if (isNew = record == null) {
            record = new PersistentRecord();
            record.setKey(key);
        }
        record.setDatabase(database).setClassName(documentClass.getName());
        if (isNew) {
            //new record detected
            initializer.accept(recordView.cast(record));
            record.save();
        } else
            database.reload(record);
        return recordView.cast(record);
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
        PersistentRecord record = getRecord(key, PersistentRecord::new).orElse(null);
        final boolean isNew;
        if (isNew = record == null) {
            record = new PersistentRecord();
            record.setKey(key);
        }
        record.setDatabase(database).setClassName(documentClass.getName());
        if (!isNew)
            database.reload(record);
        updater.accept(recordView.cast(record));
    }

    /**
     * Deletes the record associated with key.
     *
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    @Override
    public boolean delete(final Comparable<?> key) {
        return getRecord(key, id -> {
            database.delete(id.getIdentity(), ODatabase.OPERATION_MODE.SYNCHRONOUS);
            return true;
        }).orElse(false);
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return getRecord(key, id -> true).orElse(false);
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        try (final SafeCloseable ignored = withDatabase(database)) {
            documentClass.truncate();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void destroy(final ODatabase<?> database, final OClass documentClass){
        final Set<String> indexes = documentClass.getIndexes().stream().map(OIndex::getName).collect(Collectors.toSet());
        database.command(new OCommandSQL(String.format("drop class %s", documentClass.getName()))).execute(); //remove class
        //remove indexes
        for (final String indexName : indexes)
            database.command(new OCommandSQL(String.format("drop index %s", indexName))).execute();
    }

    static void destroy(final ODatabase<?> database, final String collectionName) {
        final OClass documentClass = database.getMetadata().getSchema().getClass(collectionName);
        if(documentClass != null)
            destroy(database, documentClass);
    }

    @Override
    void destroy() {
        try (final SafeCloseable ignored = withDatabase(database)) {
            destroy(database, documentClass);
        }
    }

    /**
     * Determines whether this storage supports transactions.
     *
     * @return {@literal true} if transactions are supported; otherwise, {@literal false}.
     */
    @Override
    public boolean isTransactional() {
        return true;
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
    public <R extends Record, E extends Throwable> void forEachRecord(final Class<R> recordType,
                                                                      final Predicate<? super Comparable<?>> filter,
                                                                      final EntryReader<? super Comparable<?>, ? super R, E> reader) throws E {
        try (final SafeCloseable ignored = withDatabase(database)) {
            final ORecordIteratorClass<ODocument> records = database.browseClass(documentClass.getName());
            while (records.hasNext()) {
                final ODocument document = records.next();
                final PersistentRecord record;
                if (document instanceof PersistentRecord)
                    record = (PersistentRecord) document;
                else {
                    record = new PersistentRecord(document);
                    database.reload(record);
                }
                record.lock(false);
                try {
                    final Comparable<?> key;
                    if (filter.test(key = record.getKey()))
                        if (!reader.accept(key, recordType.cast(record)))
                            return;
                } finally {
                    record.unlock();
                }
            }
        }
    }

    /**
     * Gets all keys in this storage.
     *
     * @return All keys in this storage.
     */
    @Override
    public Set<? extends Comparable<?>> keySet() {
        try (final SafeCloseable ignored = withDatabase(database)) {
            final OIndex<?> index = documentClass.getClassIndex(indexName);
            final OIndexKeyCursor cursor = index.keyCursor();
            Object key;
            final Set<Comparable<?>> result = new HashSet<>(15);
            while ((key = cursor.next(5)) instanceof OCompositeKey) {
                final OCompositeKey compositeKey = (OCompositeKey) key;
                compositeKey.getKeys().stream()
                        .filter(k -> k instanceof Comparable<?>)
                        .map(k -> (Comparable<?>) k)
                        .findFirst()
                        .ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Starts transaction.
     *
     * @param level The required level of transaction.
     * @return A new transaction scope.
     */
    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) {
        final TransactionScopeImpl transaction = new TransactionScopeImpl(database);
        switch (level) {
            case READ_COMMITTED:
                transaction.setIsolationLevel(OTransaction.ISOLATION_LEVEL.READ_COMMITTED);
                break;
            case REPEATABLE_READ:
                transaction.setIsolationLevel(OTransaction.ISOLATION_LEVEL.REPEATABLE_READ);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported isolation level %s", level));
        }
        transaction.begin();
        return transaction;
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return recordView.isAssignableFrom(PersistentRecord.class);
    }
}
