package com.bytex.snamp.cluster;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
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
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents key/value storage backed by OrientDB.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OrientKeyValueStorage /*extends ODatabaseDocumentTx implements KeyValueStorage*/ {
//    private static final class TransactionScopeImpl extends OTransactionOptimistic implements TransactionScope {
//        private TransactionScopeImpl(final ODatabaseDocumentTx iDatabase) {
//            super(iDatabase);
//        }
//    }
//
//    private static final class RecordImpl extends ODocument implements Record, MapRecordView, JsonRecordView, TextRecordView, LongRecordView, DoubleRecordView{
//        private static final Gson JSON_FORMATTER = new Gson();
//        private static final long serialVersionUID = -7040180709722600847L;
//        private static final String KEY_FIELD = "key";
//        private static final String VALUE_FIELD = "value";
//        private static final String CLASS = "SnampDocument";
//        private volatile boolean detached;
//
//        @SpecialUse
//        public RecordImpl(){
//            super(CLASS);
//        }
//
//        private RecordImpl(final OIdentifiable prototype) {
//            super(CLASS, prototype.getIdentity());
//        }
//
//        private static OClass initClass(final OSchema schema) {
//            if (schema.existsClass(RecordImpl.CLASS))
//                return schema.getClass(RecordImpl.CLASS);
//            else {
//                final OClass documentClass = schema.createClass(RecordImpl.CLASS);
//                documentClass
//                        .createProperty(RecordImpl.KEY_FIELD, OType.ANY)
//                        .createIndex(OClass.INDEX_TYPE.UNIQUE)
//                        .flush();
//                documentClass
//                        .createProperty(RecordImpl.VALUE_FIELD, OType.ANY);
//                return documentClass;
//            }
//        }
//
//        private static RecordImpl get(final OClass documentClass, final Object indexKey) {
//            final OIdentifiable identifiable = (OIdentifiable) documentClass.getClassIndex(KEY_FIELD).get(indexKey);
//            return identifiable == null ? null : new RecordImpl(identifiable);
//        }
//
//        @Override
//        public RecordState getState() {
//            if (detached)
//                return RecordState.DETACHED;
//            else if (isDirty())
//                return RecordState.DIRTY;
//            else if (isEmpty())
//                return RecordState.EMPTY;
//            else
//                return RecordState.ACTIVE;
//        }
//
//        @Override
//        public boolean detach() {
//            return detached = super.detach();
//        }
//
//        @Override
//        public RecordImpl save() {
//            super.save();
//            return this;
//        }
//
//        @Override
//        public RecordImpl reset() {
//            super.reset();
//            return this;
//        }
//
//        @Override
//        public RecordImpl delete() {
//            super.delete();
//            return this;
//        }
//
//        @Override
//        public Map<String, ?> getAsMap() {
//            final Object value = field(VALUE_FIELD);
//            if (value == null)
//                return ImmutableMap.of();
//            else if (value instanceof ODocument)
//                return ((ODocument) value).toMap();
//            else
//                return ImmutableMap.of(VALUE_FIELD, value);
//        }
//
//        @Override
//        public void setAsMap(final Map<String, ?> values) {
//            if(values.size() == 1 && values.containsKey(VALUE_FIELD))
//                field(VALUE_FIELD, values.get(VALUE_FIELD));
//            else
//                field(VALUE_FIELD, new ODocument().fromMap(values));
//        }
//
//        @Override
//        public Reader getAsJson() {
//            final Object content = field(VALUE_FIELD);
//            if(content instanceof ODocument)
//                return new StringReader(((ODocument) content).toJSON());
//            else
//                return new StringReader(JSON_FORMATTER.toJson(content));
//        }
//
//        private static Optional<?> toValue(final JsonElement value) {
//            if (value instanceof JsonObject)
//                return Optional.of(new ODocument().fromJSON(JSON_FORMATTER.toJson(value)));
//            else if(value instanceof JsonPrimitive){
//                final JsonPrimitive primitive = (JsonPrimitive) value;
//                if(primitive.isBoolean())
//                    return Optional.of(primitive.getAsBoolean());
//                else if(primitive.isString())
//                    return Optional.of(primitive.getAsString());
//                else if(primitive.isNumber())
//                    return Optional.of(primitive.getAsDouble());
//            }
//            else
//                return Optional.of(JSON_FORMATTER.toJson(value));
//            return Optional.empty();
//        }
//
//        @Override
//        public void setAsJson(final Reader value) throws IOException {
//            final Object persistentValue = toValue(JSON_FORMATTER.fromJson(value, JsonElement.class))
//                    .orElseThrow(IOException::new);
//            field(VALUE_FIELD, persistentValue);
//        }
//
//        @Override
//        public String getAsText() {
//            return field(VALUE_FIELD, String.class);
//        }
//
//        @Override
//        public void setAsText(final String value) {
//            field(VALUE_FIELD, value);
//        }
//
//        @Override
//        public long getAsLong() {
//            return field(VALUE_FIELD, long.class);
//        }
//
//        @Override
//        public void setAsLong(final long value) {
//            field(VALUE_FIELD, value);
//        }
//
//        @Override
//        public double getAsDouble() {
//            return field(VALUE_FIELD, double.class);
//        }
//
//        @Override
//        public void setAsDouble(final double value) {
//            field(VALUE_FIELD, double.class);
//        }
//    }
//
//    private static final ImmutableSet<Characteristics> CHARACTERISTICS = ImmutableSet.of(Characteristics.PERSISTENT, Characteristics.TRANSACTED);
//    private final OClass documentClass;
//
//    OrientKeyValueStorage(@Nonnull final OServer databaseServer, final String name) {
//        super(databaseServer.getStoragePath(name));
//        documentClass = RecordImpl.initClass(getMetadata().getSchema());
//    }
//
//    private <R extends Record> R getRecordImpl(final Object key, final Class<R> recordView) {
//        final RecordImpl result = RecordImpl.get(documentClass, key);
//        return result == null ? null : recordView.cast(result);
//    }
//
//    /**
//     * Gets record associated with the specified key.
//     *
//     * @param key        The key of the record.
//     * @param recordView
//     * @return Selector for records in this storage.
//     */
//    @Override
//    public <R extends Record> R getRecord(final long key, final Class<R> recordView) {
//        return getRecordImpl(key, recordView);
//    }
//
//    /**
//     * Gets record associated with the specified key.
//     *
//     * @param key        The key of the record.
//     * @param recordView
//     * @return Selector for records in this storage.
//     */
//    @Override
//    public <R extends Record> R getRecord(final double key, final Class<R> recordView) {
//        return getRecordImpl(key, recordView);
//    }
//
//    /**
//     * Gets record associated with the specified key.
//     *
//     * @param key        The key of the record.
//     * @param recordView
//     * @return Selector for records in this storage.
//     */
//    @Override
//    public <R extends Record> R getRecord(final String key, final Class<R> recordView) {
//        return getRecordImpl(key, recordView);
//    }
//
//    /**
//     * Gets record associated with the specified key.
//     *
//     * @param key        The key of the record.
//     * @param recordView
//     * @return Selector for records in this storage.
//     */
//    @Override
//    public <R extends Record> R getRecord(final Instant key, final Class<R> recordView) {
//        return getRecord(key.toEpochMilli(), recordView);
//    }
//
//    /**
//     * Gets stream over all records in this storage.
//     *
//     * @param recordType Type of the record view.
//     * @return Stream of records.
//     */
//    @Override
//    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
//        final Iterator<R> records = Iterators.transform(browseClass(documentClass.getName()), proto -> recordType.cast(new RecordImpl(proto)));
//        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(records, Spliterator.IMMUTABLE), false);
//    }
//
//    /**
//     * Gets characteristics of this storage.
//     *
//     * @return Characteristics of this storage.
//     */
//    @Override
//    public ImmutableSet<Characteristics> getCharacteristics() {
//        return CHARACTERISTICS;
//    }
//
//    /**
//     * Starts transaction.
//     *
//     * @param level The required level of transaction.
//     * @return A new transaction scope.
//     */
//    @Override
//    public TransactionScope beginTransaction(final IsolationLevel level) {
//        final TransactionScopeImpl transaction = new TransactionScopeImpl(this);
//        switch (level) {
//            case READ_COMMITTED:
//                transaction.setIsolationLevel(OTransaction.ISOLATION_LEVEL.READ_COMMITTED);
//                break;
//            case REPEATABLE_READ:
//                transaction.setIsolationLevel(OTransaction.ISOLATION_LEVEL.REPEATABLE_READ);
//                break;
//            default:
//                throw new IllegalArgumentException(String.format("Unsupported isolation level %s", level));
//        }
//        transaction.begin();
//        return transaction;
//    }
//
//    @Override
//    public boolean isViewSupported(final Class<? extends Record> recordView) {
//        return recordView.isAssignableFrom(RecordImpl.class);
//    }
}
