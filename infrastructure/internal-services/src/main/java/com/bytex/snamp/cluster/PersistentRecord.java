package com.bytex.snamp.cluster;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents persistent record,
 */
final class PersistentRecord extends ODocument implements KeyValueStorage.Record, KeyValueStorage.MapRecordView, KeyValueStorage.JsonRecordView, KeyValueStorage.TextRecordView, KeyValueStorage.LongRecordView, KeyValueStorage.DoubleRecordView, KeyValueStorage.SerializableRecordView {
    private static final Gson JSON_FORMATTER = new Gson();
    private static final long serialVersionUID = -7040180709722600847L;

    private volatile boolean detached;
    private transient ODatabaseDocumentInternal database;

    @SpecialUse
    public PersistentRecord() {
    }

    PersistentRecord(final OIdentifiable prototype) {
        super(prototype.getIdentity());
    }

    void setDatabase(final ODatabaseDocumentInternal value) {
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

    void setKey(final Comparable<?> key) {
        PersistentRecordFieldDefinition.setKey(key, this);
    }

    static OClass initClass(final OSchema schema, final String name) {
        if (schema.existsClass(name))
            return schema.getClass(name);
        else {
            final OClass documentClass = schema.createClass(name);
            PersistentRecordFieldDefinition.defineFields(documentClass);
            PersistentRecordFieldDefinition.createIndex(documentClass);
            return documentClass;
        }
    }

    static <V> V get(final OClass documentClass, final Comparable<?> indexKey, final Function<? super OIdentifiable, ? extends V> transform) {
        final OIdentifiable identifiable = (OIdentifiable) documentClass.getClassIndex(PersistentRecordFieldDefinition.INDEX_NAME).get(PersistentRecordFieldDefinition.getCompositeKey(indexKey));
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

    private void saveValue(final Object value) {
        PersistentRecordFieldDefinition.VALUE.setField(value, this);
        save();
    }

    @Override
    public Serializable getValue() {
        final Object result = PersistentRecordFieldDefinition.VALUE.getField(this);
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
        final Object value = PersistentRecordFieldDefinition.VALUE.getField(this);
        if (value == null)
            return ImmutableMap.of();
        else if (value instanceof ODocument)
            return ((ODocument) value).toMap();
        else
            return ImmutableMap.of(PersistentRecordFieldDefinition.VALUE.fieldName, value);
    }

    @Override
    public void setAsMap(final Map<String, ?> values) {
        saveValue(new ODocument().fromMap(values));
    }

    @Override
    public Reader getAsJson() {
        final Object content = PersistentRecordFieldDefinition.VALUE.getField(this);
        if (content instanceof ODocument)
            return new StringReader(((ODocument) content).toJSON());
        else
            return new StringReader(JSON_FORMATTER.toJson(content));
    }

    private static Optional<?> toValue(final JsonElement value) {
        if (value instanceof JsonObject)
            return Optional.of(new ODocument().fromJSON(JSON_FORMATTER.toJson(value)));
        else if (value instanceof JsonPrimitive) {
            final JsonPrimitive primitive = (JsonPrimitive) value;
            if (primitive.isBoolean())
                return Optional.of(primitive.getAsBoolean());
            else if (primitive.isString())
                return Optional.of(primitive.getAsString());
            else if (primitive.isNumber())
                return Optional.of(primitive.getAsDouble());
        } else
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
        return PersistentRecordFieldDefinition.VALUE.getField(this).toString();
    }

    @Override
    public void setAsText(final String value) {
        saveValue(value);
    }

    @Override
    public long getAsLong() {
        return Convert.toLong(PersistentRecordFieldDefinition.VALUE.getField(this));
    }

    @Override
    public void setAsLong(final long value) {
        saveValue(value);
    }

    @Override
    public double getAsDouble() {
        return Convert.toDouble(PersistentRecordFieldDefinition.VALUE.getField(this));
    }

    @Override
    public void setAsDouble(final double value) {
        saveValue(value);
    }
}
