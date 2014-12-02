package com.itworks.snamp.mapping;

import com.itworks.snamp.WriteOnceRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Represents record set where each record is identified by unique key.
 * @param <K> Type of the record key.
 * @param <V> Type of the record content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class KeyedRecordSet<K, V> implements RecordSet<K, V> {

    private static abstract class ProxyKeyedRecordSet<K, V> extends KeyedRecordSet<K, V>{
        protected final KeyedRecordSet<K, V> recordSet;

        private ProxyKeyedRecordSet(final KeyedRecordSet<K, V> rs){
            recordSet = rs;
        }

        @Override
        protected final Set<K> getKeys() {
            return recordSet.getKeys();
        }

        @Override
        protected final V getRecord(final K key) {
            return recordSet.getRecord(key);
        }

        @Override
        public final KeyedRecordSet<K, V> parallel(final ExecutorService executor) {
            return recordSet.parallel(executor);
        }

        @Override
        public final KeyedRecordSet<K, V> sequential() {
            return recordSet.sequential();
        }

        @Override
        protected final void forEachInterrupted(final InterruptedException e) {
            recordSet.forEachInterrupted(e);
        }

        @Override
        public abstract <E extends Exception> void forEach(final RecordReader<? super K, ? super V, E> reader) throws E;
    }

    /**
     * Gets a set of available keys.
     * @return A set of available keys.
     */
    protected abstract Set<K> getKeys();

    /**
     * Gets a record identified by the specified key.
     * @param key A key which uniquely identifies the record.
     * @return A record identified by the specified key.
     */
    protected abstract V getRecord(final K key);

    /**
     * Executes reader sequentially through records in this set.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader The record reader.
     * @throws E An exception occurred in the reader.
     */
    protected final <E extends Exception> void forEachSequential(final RecordReader<? super K, ? super V, E> reader) throws E{
        for(final K key: getKeys())
            reader.read(key, getRecord(key));
    }

    /**
     * Executes reader through records in this set in parallel.
     * @param reader The record reader.
     * @param executor An executor used to schedule readers.
     * @param <E> Type of the exception that may be thrown by reader.
     * @throws E An exception occurred in the reader.
     */
    protected final <E extends Exception> void forEachParallel(final RecordReader<? super K, ? super V, E> reader,
                                         final ExecutorService executor) throws E {
        final List<Callable<Void>> tasks = new ArrayList<>(size());
        final WriteOnceRef<Exception> fault = new WriteOnceRef<>(null);
        for (final K key : getKeys())
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        reader.read(key, getRecord(key));
                    }
                    catch (final Exception e){
                        fault.set(e);
                        throw e;
                    }
                    return null;
                }
            });
        //wait for completion
        try {
            executor.invokeAll(tasks);
            RecordReaderUtils.checkAndThrow(reader, fault.get());
        }
        catch (final InterruptedException e) {
            forEachInterrupted(e);
        }
    }

    /**
     * Invokes when parallel execution fails.
     * @param e Interruption execution.
     */
    protected void forEachInterrupted(final InterruptedException e){

    }

    /**
     * Executes reader through all records in this set.
     * <p>
     *     In the default implementation this method calls {@link #forEachSequential(RecordReader)}.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader An object that accepts the record. Cannot be {@literal null}.
     * @throws E Unable to read records.
     */
    @Override
    public <E extends Exception> void forEach(final RecordReader<? super K, ? super V, E> reader) throws E {
        forEachSequential(reader);
    }

    /**
     * Gets the size of this record set.
     *
     * @return The size of this record set.
     */
    @Override
    public final int size() {
        return getKeys().size();
    }

    /**
     * Returns an equivalent object that is parallel.
     * May return itself, either because the object was already parallel,
     * or because the underlying object state was modified to be parallel.
     *
     * @param executor An executor used to execute methods in parallel manner.
     * @return An object that supports parallel execution of some methods.
     */
    @Override
    public KeyedRecordSet<K, V> parallel(final ExecutorService executor) {
        return parallel(this, executor);
    }

    private static <K, V> ProxyKeyedRecordSet<K, V> parallel(final KeyedRecordSet<K, V> parent,
                                                             final ExecutorService executor){
        return new ProxyKeyedRecordSet<K, V>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super K, ? super V, E> reader) throws E {
                recordSet.forEachParallel(reader, executor);
            }
        };
    }

    private static <K, V> ProxyKeyedRecordSet<K, V> sequential(final KeyedRecordSet<K, V> parent){
        return new ProxyKeyedRecordSet<K, V>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super K, ? super V, E> reader) throws E {
                recordSet.forEachSequential(reader);
            }
        };
    }

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    public KeyedRecordSet<K, V> sequential() {
        return sequential(this);
    }
}
