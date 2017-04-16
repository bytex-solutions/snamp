package com.bytex.snamp.cluster;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.KeyValueStorage;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

/**
 * Represents record in document-oriented database OrientDB.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class PersistentRecord extends ODocument implements KeyValueStorage.Record, KeyValueStorage.MapRecordView, KeyValueStorage.JsonRecordView, KeyValueStorage.TextRecordView, KeyValueStorage.LongRecordView, KeyValueStorage.DoubleRecordView, KeyValueStorage.SerializableRecordView {
    private static final long serialVersionUID = -7040180709722600847L;

    private volatile transient boolean detached;
    private transient ODatabaseDocumentInternal database;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public PersistentRecord() {
    }

    PersistentRecord(final OIdentifiable prototype) {
        super(prototype.getIdentity());
    }

    void setDatabase(final ODatabaseDocumentInternal value) {
        this.database = Objects.requireNonNull(value);
        _recordFormat = value.getSerializer();
    }

    Comparable<?> getKey(){
        return PersistentFieldDefinition.getKey(this);
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

    void setKey(final Comparable<?> key) {
        PersistentFieldDefinition.setKey(key, this);
    }

    /**
     * Synchronize record stored at server with local version of this record.
     */
    @Override
    public void refresh() {
        DBUtils.runWithDatabase(database, this::reload);
    }

    /**
     * Detaches all the connected records. If new records are linked to the document the detaching cannot be completed and false will
     * be returned. RidBag types cannot be fully detached when the database is connected using "remote" protocol.
     *
     * @return true if the record has been detached, otherwise false
     */
    @Override
    public boolean detach() {
        return detached = DBUtils.supplyWithDatabase(database, super::detach);
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
    public Serializable getValue() {
        return (Serializable) PersistentFieldDefinition.RAW_VALUE.getField(this);
    }

    private void saveField(final PersistentFieldDefinition field, final Object value) {
        if (!field.setField(value, this))
            throw new IllegalArgumentException(String.format("Value %s is incompatible with field %s", value, field));
        else
            DBUtils.runWithDatabase(database, this::save);
    }

    @Override
    public void setValue(final Serializable value) {
        saveField(PersistentFieldDefinition.RAW_VALUE, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ?> getAsMap() {
        return (Map<String, ?>) PersistentFieldDefinition.MAP_VALUE.getField(this);
    }

    @Override
    public void setAsMap(final Map<String, ?> values) {
        saveField(PersistentFieldDefinition.MAP_VALUE, values);
    }

    @Override
    public Reader getAsJson() {
        return (Reader) PersistentFieldDefinition.JSON_DOCUMENT_VALUE.getField(this);
    }

    @Override
    public void setAsJson(final Reader value) throws IOException {
        saveField(PersistentFieldDefinition.JSON_DOCUMENT_VALUE, value);
    }

    @Override
    public StringWriter createJsonWriter() {
        return new StringWriter(512) {
            @Override
            public void close() throws IOException {
                saveField(PersistentFieldDefinition.JSON_DOCUMENT_VALUE, getBuffer());
                super.close();
            }
        };
    }

    @Override
    public String getAsText() {
        return (String) PersistentFieldDefinition.TEXT_VALUE.getField(this);
    }

    @Override
    public void setAsText(final String value) {
        saveField(PersistentFieldDefinition.TEXT_VALUE, value);
    }

    @Override
    public long getAsLong() {
        return Convert.toLong(PersistentFieldDefinition.LONG_VALUE.getField(this))
                .orElseThrow(NumberFormatException::new);
    }

    @Override
    public void accept(final long value) {
        saveField(PersistentFieldDefinition.LONG_VALUE, value);
    }

    @Override
    public double getAsDouble() {
        return Convert.toDouble(PersistentFieldDefinition.DOUBLE_VALUE.getField(this))
                .orElseThrow(NumberFormatException::new);
    }

    @Override
    public void accept(final double value) {
        saveField(PersistentFieldDefinition.DOUBLE_VALUE, value);
    }
}
