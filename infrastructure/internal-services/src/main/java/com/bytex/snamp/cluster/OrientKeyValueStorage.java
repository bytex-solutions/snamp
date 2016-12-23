package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.core.KeyValueStorage;
import com.google.common.collect.Iterators;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents key/value storage backed by OrientDB.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OrientKeyValueStorage extends GridSharedObject implements KeyValueStorage {
    private static final class TransactionScopeImpl extends OTransactionOptimistic implements TransactionScope {
        private TransactionScopeImpl(final ODatabaseDocumentTx iDatabase) {
            super(iDatabase);
        }
    }

    private volatile OClass documentClass;
    private final ODatabaseDocumentTx database;
    private final String indexName;

    OrientKeyValueStorage(final ODatabaseDocumentTx database,
                          final String collectionName,
                          final boolean forceCreate) {
        ODatabaseRecordThreadLocal.INSTANCE.set(this.database = Objects.requireNonNull(database));
        indexName = collectionName + "Index";
        //init class
        final OSchema schema = database.getMetadata().getSchema();
        if (schema.existsClass(collectionName))
            documentClass = schema.getClass(collectionName);
        else if (forceCreate) {
            documentClass = schema.createClass(collectionName);
            PersistentFieldDefinition.defineFields(documentClass);
            PersistentFieldDefinition.createIndex(documentClass, indexName);
        } else
            documentClass = null;
        ODatabaseRecordThreadLocal.INSTANCE.remove();
    }

    @Override
    boolean isDestroyed(){
        return documentClass == null;
    }

    private OClass getDocumentClass(){
        if(isDestroyed())
            throw objectIsDestroyed();
        else
            return documentClass;
    }

    private <V> V getRecord(final Comparable<?> indexKey, final Function<? super OIdentifiable, ? extends V> transform) {
        final OClass documentClass = getDocumentClass();
        final OIdentifiable recordId = DBUtils.supplyWithDatabase(database, () -> (OIdentifiable) documentClass.getClassIndex(indexName).get(PersistentFieldDefinition.getCompositeKey(indexKey)));
        return recordId == null ? null : transform.apply(recordId);
    }

    @Override
    public String getName() {
        return getDocumentClass().getName();
    }

    /**
     * Determines whether this service is backed by persistent storage.
     *
     * @return {@literal true}, if this service is backed by persistent storage; otherwise, {@literal false}.
     */
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
        final PersistentRecord record = getRecord(key, PersistentRecord::new);
        if (record != null) {
            record.setDatabase(database);
            record.setClassName(getDocumentClass().getName());
            return database.load(record) == null ? Optional.empty() : Optional.of(record).map(recordView::cast);
        } else
            return Optional.empty();
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
        PersistentRecord record = getRecord(key, PersistentRecord::new);
        final boolean isNew;
        if (isNew = record == null) {
            record = new PersistentRecord();
            record.setKey(key);
        }
        record.setDatabase(database);
        record.setClassName(getDocumentClass().getName());
        if (isNew) {
            //new record detected
            initializer.accept(recordView.cast(record));
        } else
            database.reload(record);
        return recordView.cast(record);
    }

    /**
     * Deletes the record associated with key.
     *
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    @Override
    public boolean delete(final Comparable<?> key) {
        final ORID recordId = getRecord(key, OIdentifiable::getIdentity);
        final boolean success;
        if (success = recordId != null)
            DBUtils.runWithDatabase(database, () -> database.delete(recordId, ODatabase.OPERATION_MODE.SYNCHRONOUS));
        return success;
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return getRecord(key, OIdentifiable::getIdentity) != null;
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        final OClass documentClass = this.getDocumentClass();
        ODatabaseRecordThreadLocal.INSTANCE.set(database);
        try {
            documentClass.truncate();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }

    private void destroyImpl() {
        final OClass documentClass = this.documentClass;
        this.documentClass = null;
        if (documentClass != null) {
            database.command(new OCommandSQL(String.format("drop class %s", documentClass.getName()))).execute(); //remove class
            database.command(new OCommandSQL(String.format("drop index %s", indexName))).execute();       //remove indexes
        }
    }

    @Override
    void destroy() {
        DBUtils.runWithDatabase(database, this::destroyImpl);
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
     * Gets stream over all records in this storage.
     *
     * @param recordType Type of the record view.
     * @return Stream of records.
     */
    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        final OClass documentClass = getDocumentClass();
        final Iterator<R> records = Iterators.transform(database.browseClass(documentClass.getName()), proto -> recordType.cast(new PersistentRecord(proto)));
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(records, Spliterator.IMMUTABLE), false);
    }

    /**
     * Starts transaction.
     *
     * @param level The required level of transaction.
     * @return A new transaction scope.
     */
    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) {
        getDocumentClass();
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
