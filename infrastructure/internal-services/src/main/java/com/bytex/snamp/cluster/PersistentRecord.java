package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.io.*;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.cluster.DBUtils.withDatabase;

/**
 * Represents record in document-oriented database OrientDB.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
final class PersistentRecord extends ODocument implements KeyValueStorage.Record, KeyValueStorage.MapRecordView, KeyValueStorage.JsonRecordView, KeyValueStorage.TextRecordView, KeyValueStorage.LongRecordView, KeyValueStorage.DoubleRecordView, KeyValueStorage.SerializableRecordView {
    private static final long serialVersionUID = -7040180709722600847L;
    private static final String VALUE_FIELD = "value";

    private transient boolean detached;
    private transient ODatabaseDocumentInternal database;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public PersistentRecord() {
    }

    PersistentRecord(final Comparable<?> key) {
        RecordKey.create(key).setKey(this);
    }

    PersistentRecord(final OIdentifiable prototype) {
        super(prototype.getIdentity());
    }

    PersistentRecord setDatabase(final ODatabaseDocumentInternal value) {
        this.database = Objects.requireNonNull(value);
        _recordFormat = value.getSerializer();
        return this;
    }

    Comparable<?> getKey() {
        return new RecordKey(this).get();
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

    @Override
    protected void checkForLoading() {
        if (_status == ORecordElement.STATUS.NOT_LOADED && database != null)
            reload(null, true);
    }

    /**
     * Synchronize record stored at server with local version of this record.
     */
    @Override
    public void refresh() {
        try(final SafeCloseable ignored = withDatabase(database)){
            reload();
        }
    }

    /**
     * Detaches all the connected records. If new records are linked to the document the detaching cannot be completed and false will
     * be returned. RidBag types cannot be fully detached when the database is connected using "remote" protocol.
     *
     * @return true if the record has been detached, otherwise false
     */
    @Override
    public boolean detach() {
        try (final SafeCloseable ignored = withDatabase(database)) {
            return super.detach();
        } finally {
            detached = true;
        }
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

    private void saveValue(final Object fieldValue){
        field(VALUE_FIELD, Objects.requireNonNull(fieldValue)).save();
    }

    @Override
    public Serializable getValue() {
        return field(VALUE_FIELD);
    }

    @Override
    public void setValue(final Serializable value) {
        saveValue(value);
    }

    @Override
    public Map<String, ?> getAsMap() {
        return field(VALUE_FIELD, Map.class);
    }

    @Override
    public void setAsMap(final Map<String, ?> values) {
        saveValue(values);
    }

    @Override
    public Reader getAsJson() {
        return new StringReader(this.<ODocument>field(VALUE_FIELD, ODocument.class).toJSON("prettyPrint"));
    }

    @Override
    public void setAsJson(final Reader value) throws IOException {
        final ODocument jsonDocument = new ODocument().fromJSON(IOUtils.toString(value));
        saveValue(jsonDocument);
    }

    @Override
    public StringWriter createJsonWriter() {
        return new StringWriter(512) {
            @Override
            public void close() throws IOException {
                saveValue(getBuffer());
                super.close();
            }
        };
    }

    @Override
    public String getAsText() {
        return field(VALUE_FIELD, String.class);
    }

    @Override
    public void setAsText(final String value) {
        saveValue(value);
    }

    @Override
    public long getAsLong() {
        return field(VALUE_FIELD, long.class);
    }

    @Override
    public void accept(final long value) {
        saveValue(value);
    }

    @Override
    public double getAsDouble() {
        return field(VALUE_FIELD, double.class);
    }

    @Override
    public void accept(final double value) {
        saveValue(value);
    }

    static void defineFields(final OClass documentClass) {
        RecordKey.defineFields(documentClass);
        if (!documentClass.existsProperty(VALUE_FIELD))
            documentClass.createProperty(VALUE_FIELD, OType.ANY)
                    .setNotNull(false)
                    .setDescription("Field with value");
    }
}
