package com.bytex.snamp.core;

import com.bytex.snamp.Convert;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Represents non-persistent key/value storage.
 */
final class LocalKeyValueStorage implements KeyValueStorage {
    private static final ImmutableSet<Characteristics> CHARACTERISTICS = ImmutableSet.of();

    private static final class ReadOnlyMap extends HashMap<String, Object>{
        private static final long serialVersionUID = 5529650287002423883L;

        private ReadOnlyMap(final Map<String, ?> values){
            super(values);
        }

        @Override
        public Object put(final String key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object putIfAbsent(final String key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(final String key, final Object oldValue, final Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object replace(final String key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object merge(final String key, final Object value, final BiFunction<? super Object, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(final BiFunction<? super String, ? super Object, ?> function) {
            throw new UnsupportedOperationException();
        }
    }

    private final class InMemoryRecord implements Record, MapRecordView, JsonRecordView, LongRecordView, DoubleRecordView, TextRecordView{
        private volatile Object value;


        @Override
        public void save() {

        }

        @Override
        public void clear() {

        }

        @Override
        public void reset() {

        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public boolean delete() {
            return false;
        }

        @Override
        public RecordState getState() {
            return null;
        }

        @Override
        public Map<String, ?> getAsMap() {
            final Object v = value;
            return v instanceof ReadOnlyMap ? (ReadOnlyMap) v : ImmutableMap.of();
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            this.value = new ReadOnlyMap(values);
        }

        @Override
        public String getAsText() {
            return Objects.toString(value, "");
        }

        @Override
        public void setAsText(final String value) {
            this.value = value;
        }

        @Override
        public StringReader getAsJson() {
            return new StringReader(getAsText());
        }

        @Override
        public void setAsJson(final Reader value) throws IOException {
            setAsText(IOUtils.toString(value));
        }

        @Override
        public long getAsLong() {
            return Convert.toLong(value);
        }

        @Override
        public void setAsLong(final long value) {
            this.value = value;
        }

        @Override
        public double getAsDouble() {
            return Convert.toDouble(value);
        }

        @Override
        public void setAsDouble(final double value) {
            this.value = value;
        }
    }

    private final ConcurrentMap<Long, InMemoryRecord> records = new ConcurrentHashMap<>();

    private InMemoryRecord getRecord(final long key){
        return null;
    }

    @Override
    public <R extends Record> R getRecord(final long key, Class<R> recordView) {
        return null;
    }

    @Override
    public <R extends Record> R getRecord(final double key, Class<R> recordView) {
        return null;
    }

    @Override
    public <R extends Record> R getRecord(final String key, Class<R> recordView) {
        return null;
    }

    @Override
    public <R extends Record> R getRecord(final Instant key, Class<R> recordView) {
        return null;
    }

    @Override
    public <V extends Record> Stream<V> getRecords(final Class<V> recordType) {
        return null;
    }

    @Override
    public ImmutableSet<Characteristics> getCharacteristics() {
        return CHARACTERISTICS;
    }

    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Transactions are not supported");
    }
}
