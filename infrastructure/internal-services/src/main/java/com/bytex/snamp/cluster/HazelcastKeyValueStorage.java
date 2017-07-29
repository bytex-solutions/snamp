package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Represents non-persistent high-performance key/value storage backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.1
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
            return Convert.toLong(getValue()).orElseThrow(NumberFormatException::new);
        }

        @Override
        public void accept(final long value) {
            setValue(value);
        }

        @Override
        public double getAsDouble() {
            return Convert.toDouble(getValue()).orElseThrow(NumberFormatException::new);
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

        @Override
        public StringWriter createJsonWriter() {
            return new StringWriter(1024){
                @Override
                public void close() throws IOException {
                    setAsText(getBuffer().toString());
                    super.close();
                }
            };
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
        final HazelcastRecord record = getDistributedObject().containsKey(key) ? new HazelcastRecord(getDistributedObject(), key) : null;
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
        final HazelcastRecord record = new HazelcastRecord(getDistributedObject(), key);
        if (!record.isInitialized()) {
            getDistributedObject().lock(key);
            try {
                if (!record.isInitialized())
                    initializer.accept(recordView.cast(record));
            } finally {
                getDistributedObject().unlock(key);
            }
        }
        return recordView.cast(record);
    }

    /**
     * Updates or creates record associated with the specified key.
     *
     * @param key        The key of the record.
     * @param recordView Type of the record representation.
     * @param updater    Record updater.
     * @throws E Unable to update record.
     */
    @Override
    public <R extends Record, E extends Throwable> void updateOrCreateRecord(final Comparable<?> key, final Class<R> recordView, final Acceptor<? super R, E> updater) throws E {
        updater.accept(recordView.cast(new HazelcastRecord(getDistributedObject(), key)));
    }

    /**
     * Deletes the record associated with key.
     *
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    @Override
    public boolean delete(final Comparable<?> key) {
        return getDistributedObject().remove(key) != null;
    }

    /**
     * Gets all keys in this storage.
     *
     * @return All keys in this storage.
     */
    @Override
    public Set<? extends Comparable<?>> keySet() {
        return getDistributedObject().keySet();
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return getDistributedObject().containsKey(key);
    }

    /**
     * Iterates over records.
     *
     * @param recordType Type of the record representation.
     * @param filter     Query filter. Cannot be {@literal null}.
     * @param reader     Record reader. Cannot be {@literal null}.
     * @throws E Reading failed.
     */
    @Override
    public <R extends Record, E extends Throwable> void forEachRecord(final Class<R> recordType, final Predicate<? super Comparable<?>> filter, final EntryReader<? super Comparable<?>, ? super R, E> reader) throws E {
        for (final Comparable<?> key : getDistributedObject().keySet())
            if (filter.test(key)) {
                getDistributedObject().lock(key);
                try {
                    if (!reader.accept(key, recordType.cast(new HazelcastRecord(getDistributedObject(), key))))
                        return;
                } finally {
                    getDistributedObject().unlock(key);
                }
            }
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        getDistributedObject().clear();
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
