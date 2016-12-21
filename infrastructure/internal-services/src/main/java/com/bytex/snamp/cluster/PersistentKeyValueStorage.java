package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents key/value storage backed by OrientDB.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class PersistentKeyValueStorage implements KeyValueStorage {

    private static final class TransactionScopeImpl extends OTransactionOptimistic implements TransactionScope {
        private TransactionScopeImpl(final ODatabaseDocumentTx iDatabase) {
            super(iDatabase);
        }
    }

    private enum FieldDefinition{
        DECIMAL_KEY(OType.DECIMAL, "nKey", true, true) {
            @Override
            Optional<Number> getKey(final Comparable<?> key) {
                return key instanceof Number ? Optional.of((Number) key) : Optional.empty();
            }
        },
        STRING_KEY(OType.STRING, "sKey", true, true) {
            @Override
            Optional<String> getKey(final Comparable<?> key) {
                if(key instanceof String)
                    return Optional.of(key.toString());
                else if(key instanceof Enum<?>)
                    return Optional.of(((Enum<?>) key).name());
                else
                    return Optional.empty();
            }
        },
        DATE_KEY(OType.DATE, "dKey", true, true) {
            @Override
            Optional<Date> getKey(final Comparable<?> key) {
                if(key instanceof Date)
                    return Optional.of((Date) key);
                else if(key instanceof Instant)
                    return Optional.of(Date.from((Instant) key));
                else
                    return Optional.empty();
            }
        },
        VALUE(OType.ANY, "value", false, true) {
            @Override
            Optional<?> getKey(final Comparable<?> key) {
                return Optional.empty();
            }
        };

        private static final String INDEX_NAME = "SnampIndex";
        private static final ImmutableSortedSet<FieldDefinition> ALL_FIELDS = ImmutableSortedSet.copyOf(values());
        private static final ImmutableSortedSet<FieldDefinition> INDEX_FIELDS = ImmutableSortedSet.copyOf(ALL_FIELDS.stream().filter(f -> f.isIndex).iterator());
        private final OType fieldType;
        private final String fieldName;
        private final boolean isIndex;
        private final boolean notNull;

        FieldDefinition(final OType type, final String fieldName, final boolean index, final boolean notNull){
            this.fieldType = type;
            this.fieldName = fieldName;
            this.isIndex = index;
            this.notNull = notNull;
        }

        abstract Optional<?> getKey(final Comparable<?> key);

        static List<?> getCompositeKey(final Comparable<?> key) {
            final List<Object> keys = new LinkedList<>();
            for (final FieldDefinition index : INDEX_FIELDS) {
                final Optional<?> value = index.getKey(key);
                keys.add(value.orElseGet(() -> null));
            }
            return keys;
        }

        static void setKey(final Comparable<?> key, final ODocument document) {
            for (final FieldDefinition index : INDEX_FIELDS) {
                final Optional<?> value = index.getKey(key);
                if (value.isPresent()) {
                    index.setField(value.get(), document);
                    return;
                }
            }
            throw new IllegalArgumentException(String.format("Unsupported key %s", key));
        }

        private void setField(final Object fieldValue, final ODocument document){
            document.field(fieldName, fieldValue);
        }

        private Object getField(final ODocument document) {
            return document.field(fieldName, fieldType);
        }

        private void registerProperty(final OClass documentClass){
            documentClass.createProperty(fieldName, fieldType).setNotNull(notNull);
        }

        static void defineFields(final OClass documentClass) {
            for (final FieldDefinition field : ALL_FIELDS)
                field.registerProperty(documentClass);
        }

        static void createIndex(final OClass documentClass) {
            documentClass.createIndex(INDEX_NAME, OClass.INDEX_TYPE.DICTIONARY_HASH_INDEX, INDEX_FIELDS.stream().map(f -> f.fieldName).toArray(String[]::new));
        }

        @Override
        public String toString() {
            return fieldName;
        }
    }

    private static final class PersistentRecord extends ODocument implements Record, MapRecordView, JsonRecordView, TextRecordView, LongRecordView, DoubleRecordView, SerializableRecordView{
        private static final Gson JSON_FORMATTER = new Gson();
        private static final long serialVersionUID = -7040180709722600847L;

        private volatile boolean detached;
        private transient ODatabaseDocumentInternal database;

        @SpecialUse
        public PersistentRecord() {
        }

        private PersistentRecord(final OIdentifiable prototype) {
            super(prototype.getIdentity());
        }

        private void setDatabase(final ODatabaseDocumentInternal value) {
            this.database = Objects.requireNonNull(value);
            _recordFormat = value.getSerializer();
        }

        @Override
        public ODatabaseDocumentInternal getDatabase() {
            return database;
        }

        @Override
        public ODatabaseDocument getDatabaseIfDefined() {
            return database;
        }

        @Override
        protected ODatabaseDocumentInternal getDatabaseInternal() {
            return database;
        }

        @Override
        protected ODatabaseDocumentInternal getDatabaseIfDefinedInternal() {
            return database;
        }

        private void setKey(final Comparable<?> key) {
            FieldDefinition.setKey(key, this);
        }

        private static OClass initClass(final OSchema schema, final String name) {
            if (schema.existsClass(name))
                return schema.getClass(name);
            else {
                final OClass documentClass = schema.createClass(name);
                FieldDefinition.defineFields(documentClass);
                FieldDefinition.createIndex(documentClass);
                return documentClass;
            }
        }

        private static <V> V get(final OClass documentClass, final Comparable<?> indexKey, final Function<? super OIdentifiable, ? extends V> transform) {
            final OIdentifiable identifiable = (OIdentifiable) documentClass.getClassIndex(FieldDefinition.INDEX_NAME).get(FieldDefinition.getCompositeKey(indexKey));
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

        private void saveValue(final Object value){
            FieldDefinition.VALUE.setField(value, this);
            save();
        }

        @Override
        public Serializable getValue() {
            final Object result = FieldDefinition.VALUE.getField(this);
            if (result instanceof byte[])
                try {
                    return IOUtils.deserialize((byte[]) result, Serializable.class);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            else if (result instanceof Serializable)
                return (Serializable) result;
            else
                return null;
        }

        @Override
        public void setValue(final Serializable value) {
            final byte[] bytes;
            try {
                bytes = IOUtils.serialize(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            saveValue(bytes);
        }

        @Override
        public Map<String, ?> getAsMap() {
            final Object value = FieldDefinition.VALUE.getField(this);
            if (value == null)
                return ImmutableMap.of();
            else if (value instanceof ODocument)
                return ((ODocument) value).toMap();
            else
                return ImmutableMap.of(FieldDefinition.VALUE.fieldName, value);
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            saveValue(new ODocument().fromMap(values));
        }

        @Override
        public Reader getAsJson() {
            final Object content = FieldDefinition.VALUE.getField(this);
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
            saveValue(persistentValue);
        }

        @Override
        public String getAsText() {
            return FieldDefinition.VALUE.getField(this).toString();
        }

        @Override
        public void setAsText(final String value) {
            saveValue(value);
        }

        @Override
        public long getAsLong() {
            return Convert.toLong(FieldDefinition.VALUE.getField(this));
        }

        @Override
        public void setAsLong(final long value) {
            saveValue(value);
        }

        @Override
        public double getAsDouble() {
            return Convert.toDouble(FieldDefinition.VALUE.getField(this));
        }

        @Override
        public void setAsDouble(final double value) {
            saveValue(value);
        }
    }

    private final OClass documentClass;
    private final ODatabaseDocumentTx database;

    PersistentKeyValueStorage(final ODatabaseDocumentTx database,
                              final String collectionName) {
        this.database = Objects.requireNonNull(database);
        documentClass = PersistentRecord.initClass(database.getMetadata().getSchema(), collectionName);
    }

    @Override
    public String getName() {
        return documentClass.getName();
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
                record.setDatabase(database);
                record.setClassName(documentClass.getName());
                record.load();
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
        record.setDatabase(database);
        record.setKey(key);
        record.setClassName(documentClass.getName());
        try {
            record.load();
        } catch (final ORecordNotFoundException e) {
            //new record detected
            initializer.accept(recordView.cast(record));
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
            database.delete(recordId, ODatabase.OPERATION_MODE.SYNCHRONOUS);
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
