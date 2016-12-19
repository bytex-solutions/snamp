package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;
import com.orientechnologies.orient.server.OServer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
final class PersistentKeyValueStorage extends ODatabaseDocumentTx implements KeyValueStorage {
    private static final class TransactionScopeImpl extends OTransactionOptimistic implements TransactionScope {
        private TransactionScopeImpl(final ODatabaseDocumentTx iDatabase) {
            super(iDatabase);
        }
    }

    private static final class PersistentRecord extends ODocument implements Record, MapRecordView, JsonRecordView, TextRecordView, LongRecordView, DoubleRecordView{
        private static final Gson JSON_FORMATTER = new Gson();
        private static final long serialVersionUID = -7040180709722600847L;
        private static final String KEY_FIELD = "key";
        private static final String VALUE_FIELD = "value";
        private static final String CLASS = "SnampDocument";
        private volatile boolean detached;

        @SpecialUse
        public PersistentRecord(){
            super(CLASS);
        }

        private PersistentRecord(final OIdentifiable prototype) {
            super(CLASS, prototype.getIdentity());
        }

        private void setKey(final Comparable<?> key){
            field(KEY_FIELD, key);
        }

        private static OClass initClass(final OSchema schema) {
            if (schema.existsClass(PersistentRecord.CLASS))
                return schema.getClass(PersistentRecord.CLASS);
            else {
                final OClass documentClass = schema.createClass(PersistentRecord.CLASS);
                documentClass
                        .createProperty(PersistentRecord.KEY_FIELD, OType.ANY)
                        .createIndex(OClass.INDEX_TYPE.UNIQUE)
                        .flush();
                documentClass
                        .createProperty(PersistentRecord.VALUE_FIELD, OType.ANY);
                return documentClass;
            }
        }

        private static <V> V get(final OClass documentClass, final Comparable<?> indexKey, final Function<? super OIdentifiable, ? extends V> transform) {
            final OIdentifiable identifiable = (OIdentifiable) documentClass.getClassIndex(KEY_FIELD).get(indexKey);
            return identifiable == null ? null : transform.apply(identifiable);
        }

        /**
         * Synchronize record stored at server with local version of this record.
         */
        @Override
        public void refresh() {
            load();
        }

        /**
         * Detaches all the connected records. If new records are linked to the document the detaching cannot be completed and false will
         * be returned. RidBag types cannot be fully detached when the database is connected using "remote" protocol.
         *
         * @return true if the record has been detached, otherwise false
         */
        @Override
        public boolean detach() {
            return detached = super.detach();
        }

        /**
         * Determines whether this record is no longer associated with storage.
         *
         * @return {@literal true}, if this record is detached from the storage; otherwise, {@literal false}.
         */
        @Override
        public boolean isDetached() {
            return detached;
        }

        @Override
        public Map<String, ?> getAsMap() {
            final Object value = field(VALUE_FIELD);
            if (value == null)
                return ImmutableMap.of();
            else if (value instanceof ODocument)
                return ((ODocument) value).toMap();
            else
                return ImmutableMap.of(VALUE_FIELD, value);
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            if(values.size() == 1 && values.containsKey(VALUE_FIELD))
                field(VALUE_FIELD, values.get(VALUE_FIELD));
            else
                field(VALUE_FIELD, new ODocument().fromMap(values));
        }

        @Override
        public Reader getAsJson() {
            final Object content = field(VALUE_FIELD);
            if(content instanceof ODocument)
                return new StringReader(((ODocument) content).toJSON());
            else
                return new StringReader(JSON_FORMATTER.toJson(content));
        }

        private static Optional<?> toValue(final JsonElement value) {
            if (value instanceof JsonObject)
                return Optional.of(new ODocument().fromJSON(JSON_FORMATTER.toJson(value)));
            else if(value instanceof JsonPrimitive){
                final JsonPrimitive primitive = (JsonPrimitive) value;
                if(primitive.isBoolean())
                    return Optional.of(primitive.getAsBoolean());
                else if(primitive.isString())
                    return Optional.of(primitive.getAsString());
                else if(primitive.isNumber())
                    return Optional.of(primitive.getAsDouble());
            }
            else
                return Optional.of(JSON_FORMATTER.toJson(value));
            return Optional.empty();
        }

        @Override
        public void setAsJson(final Reader value) throws IOException {
            final Object persistentValue = toValue(JSON_FORMATTER.fromJson(value, JsonElement.class))
                    .orElseThrow(IOException::new);
            field(VALUE_FIELD, persistentValue);
        }

        @Override
        public String getAsText() {
            return field(VALUE_FIELD, String.class);
        }

        @Override
        public void setAsText(final String value) {
            field(VALUE_FIELD, value);
        }

        @Override
        public long getAsLong() {
            return field(VALUE_FIELD, long.class);
        }

        @Override
        public void setAsLong(final long value) {
            field(VALUE_FIELD, value);
        }

        @Override
        public double getAsDouble() {
            return field(VALUE_FIELD, double.class);
        }

        @Override
        public void setAsDouble(final double value) {
            field(VALUE_FIELD, double.class);
        }
    }

    private final OClass documentClass;

    PersistentKeyValueStorage(@Nonnull final OServer databaseServer, final String name) {
        super(databaseServer.getStoragePath(name));
        documentClass = PersistentRecord.initClass(getMetadata().getSchema());
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
        final PersistentRecord record = PersistentRecord.get(documentClass, key, PersistentRecord::new);
        if (record != null) {
            try {
                load(record);
            } catch (final ORecordNotFoundException e) {
                return Optional.empty();
            }
            return Optional.of(record).map(recordView::cast);
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
        final PersistentRecord record = new PersistentRecord();
        record.setKey(key);
        try {
            load(record);
        } catch (final ORecordNotFoundException e) {
            //new record detected
            initializer.accept(recordView.cast(record));
            record.save(true);
        }
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
        final ORID recordId = PersistentRecord.get(documentClass, key, OIdentifiable::getIdentity);
        final boolean success;
        if (success = recordId != null)
            delete(recordId, OPERATION_MODE.SYNCHRONOUS);
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
        return PersistentRecord.get(documentClass, key, OIdentifiable::getIdentity) != null;
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        try {
            documentClass.truncate();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
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
     * Gets stream over all records in this storage.
     *
     * @param recordType Type of the record view.
     * @return Stream of records.
     */
    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        final Iterator<R> records = Iterators.transform(browseClass(documentClass.getName()), proto -> recordType.cast(new PersistentRecord(proto)));
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
        final TransactionScopeImpl transaction = new TransactionScopeImpl(this);
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
