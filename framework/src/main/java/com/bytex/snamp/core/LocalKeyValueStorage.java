package com.bytex.snamp.core;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents in-memory implementation of KV storage.
 * @since 2.0
 * @version 2.0
 */
final class LocalKeyValueStorage extends ConcurrentHashMap<Comparable<?>, KeyValueStorage.Record> implements KeyValueStorage {
    private static final long serialVersionUID = 4077365936278946842L;

    private static final class MapValue extends HashMap<String, Object>{
        private static final long serialVersionUID = 8695790321685285451L;

        private MapValue(final Map<String, ?> values){
            super(values);
        }
    }

    @NotThreadSafe
    private static final class MaterializedRecord implements Record, SerializableRecordView, TextRecordView, MapRecordView, LongRecordView, DoubleRecordView, JsonRecordView {
        private volatile boolean detached;
        private volatile Serializable value;
        private final AtomicInteger version;

        private MaterializedRecord() {
            detached = false;
            version = new AtomicInteger(0);
        }

        @Override
        public void refresh() {
        }

        @Override
        public int getVersion() {
            return version.get();
        }

        @Override
        public RecordState getState() {
            if (detached)
                return RecordState.DETACHED;
            else if (value == null)
                return RecordState.NEW;
            else
                return RecordState.ACTIVE;
        }

        @Override
        public Serializable getValue() {
            return value;
        }

        @Override
        public void setValue(final Serializable value) {
            if (detached)
                throw new IllegalStateException("Record is detached");
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public String getAsText() {
            final Serializable result = getValue();
            return result != null ? result.toString() : null;
        }

        @Override
        public void setAsText(final String value) {
            setValue(value);
        }

        @Override
        public Map<String, ?> getAsMap() {
            final Serializable value = getValue();
            return value instanceof MapValue ? (MapValue) value : ImmutableMap.of();
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            setValue(new MapValue(values));
        }

        @Override
        public long getAsLong() {
            return Convert.toLong(getValue());
        }

        @Override
        public void setAsLong(final long value) {
            setValue(value);
        }

        @Override
        public double getAsDouble() {
            return Convert.toDouble(getValue());
        }

        @Override
        public void setAsDouble(final double value) {
            setValue(value);
        }

        @Override
        public Reader getAsJson() {
            final String json = getAsText();
            return json != null && json.length() > 0 ? new StringReader(json) : IOUtils.EMPTY_READER;
        }

        @Override
        public void setAsJson(final Reader value) throws IOException {
            setAsText(IOUtils.toString(value));
        }
    }

    private final String name;

    LocalKeyValueStorage(final String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public <R extends Record> R getRecord(final Comparable<?> key, final Class<R> recordView) {
        Record record = get(key);
        if (record == null) {
            final Record newRecord;
            record = firstNonNull(putIfAbsent(key, newRecord = new MaterializedRecord()), newRecord);
        }
        return recordView.cast(record);
    }

    @Override
    public boolean delete(final Comparable<?> key) {
        return remove(key) != null;
    }

    @Override
    public boolean exists(final Comparable<?> key) {
        return containsKey(key);
    }

    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        return values().stream().map(recordType::cast);
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return recordView.isAssignableFrom(MaterializedRecord.class);
    }

    @Override
    public void close() {

    }
}
