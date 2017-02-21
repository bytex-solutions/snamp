package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
final class FileBasedKeyValueStorage extends ThreadSafeObject implements KeyValueStorage {
    private static File DATABASE_HOME = interfaceStaticInitialize(() -> {
        final String KARAF_DATA_DIR = "karaf.data";
        final File databaseHome;
        if(System.getProperties().containsKey(KARAF_DATA_DIR))
            databaseHome = Paths.get(System.getProperty(KARAF_DATA_DIR), "snamp", "localStorage").toFile();
        else
            databaseHome = Files.createTempDirectory("snamp").toFile();
        if(!databaseHome.exists()){
            final boolean created = databaseHome.mkdir();
            assert created;
        }
        return databaseHome;
    });

    private static final class MapValue extends HashMap<String, Object> {
        private static final long serialVersionUID = 8695790321685285451L;

        private MapValue(final Map<String, ?> values) {
            super(values);
        }
    }

    @ThreadSafe
    private static final class FileRecord extends ThreadSafeObject implements Record, SerializableRecordView, JsonRecordView, TextRecordView, LongRecordView, DoubleRecordView, MapRecordView{
        private static final TypeToken<Serializable> CONTENT_TYPE = TypeToken.of(Serializable.class);
        private Serializable content;
        private File contentHolder;

        private FileRecord(final File databasePath, final String name){
            this(new File(databasePath, name));
        }

        private FileRecord(final File contentHolder){
            super(SingleResourceGroup.class);
            this.contentHolder = Objects.requireNonNull(contentHolder);
        }

        private String getName(){
            return ensureActive().getName();
        }

        private File ensureActive(){
            if(contentHolder == null)
                throw new IllegalStateException("This record is detached");
            else
                return contentHolder;
        }

        private Serializable readContent() {
            try (final InputStream input = new FileInputStream(ensureActive())) {
                return IOUtils.deserialize(input, CONTENT_TYPE, getClass().getClassLoader());
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private void writeContent(final Serializable content){
            try(final OutputStream output = new FileOutputStream(ensureActive())){
                IOUtils.serialize(content, output);
            } catch (final IOException e){
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void refresh() {
            try(final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)){
                content = readContent();
            }
        }

        @Override
        public int getVersion() {
            return (int) ensureActive().lastModified();
        }

        @Override
        public boolean isDetached() {
            try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                return contentHolder == null;
            }
        }

        private void delete() {
            try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                if (contentHolder != null)
                    contentHolder.delete();
            } finally {
                contentHolder = null;
            }
        }

        @Override
        public Serializable getValue() {
            try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                return content;
            }
        }

        @Override
        public void setValue(final Serializable value) {
            try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                writeContent(content = value);
            }
        }

        @Override
        public Reader getAsJson() {
            return new StringReader(getValue().toString());
        }

        @Override
        public void setAsJson(final Reader value) throws IOException {
            setValue(IOUtils.toString(value));
        }

        @Override
        public Writer createJsonWriter() {
            return new StringWriter(512) {
                @Override
                public void close() throws IOException {
                    setValue(getBuffer().toString());
                    super.close();
                }
            };
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
        public Map<String, ?> getAsMap() {
            final Serializable value = getValue();
            return value instanceof MapValue ? (MapValue) value : ImmutableMap.of("value", value);
        }

        @Override
        public void setAsMap(final Map<String, ?> values) {
            setValue(new MapValue(values));
        }
    }

    private final File storagePath;
    private final KeyedObjects<String, FileRecord> records;

    FileBasedKeyValueStorage(final String name) {
        super(SingleResourceGroup.class);
        storagePath = Paths.get(DATABASE_HOME.getAbsolutePath(), name).toFile();
        records = AbstractKeyedObjects.create(FileRecord::getName);
        if (storagePath.exists()) {     //populate records loaded from file system
            final File[] files = storagePath.listFiles();
            if(files != null)
                for (final File contentHolder: files)
                    records.put(new FileRecord(contentHolder));
        } else if (!storagePath.mkdirs())
            throw new UncheckedIOException(new IOException(String.format("Unable to create directory %s of local key/value storage", storagePath)));
    }

    /**
     * Gets name of the distributed service.
     *
     * @return Name of this distributed service.
     */
    @Override
    public String getName() {
        return storagePath.getName();
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

    private  <R extends Record> Optional<R> getRecord(final String key, final Class<R> recordView) {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            return Optional.ofNullable(records.get(key)).map(recordView::cast);
        }
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
        return getRecord(key.toString(), recordView);
    }

    private <R extends Record, E extends Throwable> R getOrCreateRecord(final String key, final Class<R> recordView, final Acceptor<? super R, E> initializer) throws E {
        FileRecord record;
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            record = records.get(key);
        }
        if (record == null)
            try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                record = records.get(key);
                if (record == null) {
                    record = new FileRecord(storagePath, key);
                    initializer.accept(recordView.cast(record));
                    records.put(record);
                }
            }
        return recordView.cast(record);
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
        return getOrCreateRecord(key.toString(), recordView, initializer);
    }

    private  <R extends Record, E extends Throwable> void updateOrCreateRecord(final String key, final Class<R> recordView, final Acceptor<? super R, E> updater) throws E {
        FileRecord record;
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            record = records.get(key);
            if (record != null)
                updater.accept(recordView.cast(record));
        }
        if (record == null)
            try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                record = records.get(key);
                if (record == null) {
                    record = new FileRecord(storagePath, key);
                    updater.accept(recordView.cast(record));
                    records.put(record);
                } else
                    updater.accept(recordView.cast(record));
            }
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
        updateOrCreateRecord(key.toString(), recordView, updater);
    }

    private boolean delete(final String key) {
        try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            final FileRecord record = records.remove(key);
            final boolean exists;
            if (exists = record != null)
                record.delete();
            return exists;
        }
    }

    /**
     * Deletes the record associated with key.
     *
     * @param key The key to remove.
     * @return {@literal true}, if record was exist; otherwise, {@literal false}.
     */
    @Override
    public boolean delete(final Comparable<?> key) {
        return delete(key.toString());
    }

    private boolean exists(final String key){
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            return records.containsKey(key);
        }
    }

    /**
     * Determines whether the record of the specified key exists.
     *
     * @param key The key to check.
     * @return {@literal true}, if record exists; otherwise, {@literal false}.
     */
    @Override
    public boolean exists(final Comparable<?> key) {
        return exists(key.toString());
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
    public <R extends Record, E extends Throwable> void forEachRecord(final Class<R> recordType,
                                                                      final Predicate<? super Comparable<?>> filter,
                                                                      final EntryReader<? super Comparable<?>, ? super R, E> reader) throws E {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            for (final FileRecord record : records.values())
                if (filter.test(record.getName()))
                    if (!reader.accept(record.getName(), recordType.cast(record)))
                        return;
        }
    }

    /**
     * Gets all keys in this storage.
     *
     * @return All keys in this storage.
     */
    @Override
    public Set<String> keySet() {
        return records.keySet();
    }

    /**
     * Removes all record.
     */
    @Override
    public void clear() {
        try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            records.values().forEach(FileRecord::delete);
        }
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

    /**
     * Starts transaction.
     *
     * @param level The required level of transaction.
     * @return A new transaction scope.
     * @throws UnsupportedOperationException Transactions are not supported by this storage.
     */
    @Override
    public TransactionScope beginTransaction(final IsolationLevel level) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Transactions are not supported by file-based key/value storage");
    }

    @Override
    public boolean isViewSupported(final Class<? extends Record> recordView) {
        return false;
    }
}
