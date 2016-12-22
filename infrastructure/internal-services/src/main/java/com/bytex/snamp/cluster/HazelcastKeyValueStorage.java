package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents non-persistent high-performance key/value storage backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastKeyValueStorage extends HazelcastSharedObject<IMap<Comparable<?>, Serializable>> implements KeyValueStorage {

    private static final class MapValue extends HashMap<String, Object> {
        private static final long serialVersionUID = 8695790321685285451L;

        private MapValue(final Map<String, ?> values) {
            super(values);
        }
    }

    private static final class HazelcastRecord implements Record, SerializableRecordView, TextRecordView, LongRecordView, DoubleRecordView, MapRecordView, JsonRecordView {
        private final Comparable<?> recordKey;
        private final IMap<Comparable<?>, Serializable> distributedMap;
        private boolean detached;

        private HazelcastRecord(final IMap<Comparable<?>, Serializable> map, final Comparable<?> key) {
            this.distributedMap = Objects.requireNonNull(map);
            this.recordKey = Objects.requireNonNull(key);
            detached = false;
        }

        private RuntimeException detachedException(){
            return new IllegalStateException(String.format("Record with key %s is detached", recordKey));
        }

        @Override
        public void refresh() {
            if (detached)
                throw detachedException();
            distributedMap.flush();
        }

        @Override
        public int getVersion() {
            if (detached)
                throw detachedException();
            return (int) distributedMap.getLocalMapStats().getPutOperationCount();
        }

        @Override
        public boolean isDetached() {
            return detached || (detached = distributedMap.containsKey(recordKey));
        }

        @Override
        public Serializable getValue() {
            final Serializable result = distributedMap.get(recordKey);
            if (result == null) {
                detached = true;
                throw detachedException();
            } else
                return result;
        }

        private boolean isInitialized() {
            return distributedMap.containsKey(recordKey);
        }

        @Override
        public void setValue(final Serializable value) {
            if (detached)
                throw detachedException();
            distributedMap.put(recordKey, value);
        }

        @Override
        public String getAsText() {
            return getValue().toString();
        }

        @Override
        public void setAsText(final String value) {
            setValue(value);
        }

        @Override
        public long getAsLong() {
            return Convert.toLong(getValue());
        }

        @Override
        public void accept(final long value) {
            setValue(value);
        }

        @Override
        public double getAsDouble() {
            return Convert.toDouble(getValue());
        }

        @Override
        public void accept(final double value) {
            setValue(value);
        }

        @Override
        public MapValue getAsMap() {
            final Serializable value = getValue();
            return value instanceof MapValue ?
                    (MapValue) value :
                    new MapValue(ImmutableMap.of("value", value));
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            setValue(new MapValue(values));
        }

        @Override
        public Reader getAsJson() {
            return new StringReader(getAsText());
        }

        @Override
        public void setAsJson(final Reader value) throws IOException {
            setAsText(IOUtils.toString(value));
        }
    }

    HazelcastKeyValueStorage(final HazelcastInstance hazelcast, final String name) {
        super(hazelcast, name, HazelcastInstance::getMap);
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
        final HazelcastRecord record = distributedObject.containsKey(key) ? new HazelcastRecord(distributedObject, key) : null;
        return Optional.ofNullable(record).map(recordView::cast);
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
        final HazelcastRecord record = new HazelcastRecord(distributedObject, key);
        if (!record.isInitialized()) {
            distributedObject.lock(key);
            try {
                if (!record.isInitialized())
                    initializer.accept(recordView.cast(record));
            } finally {
                distributedObject.unlock(key);
            }
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
        return distributedObject.remove(key) != null;
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return distributedObject.containsKey(key);
    }

    /**
     * Gets stream over all records in this storage.
     *
     * @param recordType Type of the record view.
     * @return Stream of records.
     */
    @Override
    public <R extends Record> Stream<R> getRecords(final Class<R> recordType) {
        return distributedObject.keySet().stream().map(key -> new HazelcastRecord(distributedObject, key)).map(recordType::cast);
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        distributedObject.clear();
    }

    /**
     * Determines whether this storage supports transactions.
     *
     * @return {@literal true} if transactions are supported; otherwise, {@literal false}.
     */
    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Transaction is not supported");
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return recordView.isAssignableFrom(HazelcastRecord.class);
    }
}
