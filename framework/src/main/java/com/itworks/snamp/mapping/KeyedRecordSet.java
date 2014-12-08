package com.itworks.snamp.mapping;

import com.google.common.base.Function;
import com.itworks.snamp.WriteOnceRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Represents record set where each record is identified by unique key.
 * @param <K> Type of the record key.
 * @param <V> Type of the record content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class KeyedRecordSet<K, V> extends AbstractRecordSet<K, V> {

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
    @Override
    protected final <E extends Exception> void forEachSequential(final RecordReader<? super K, ? super V, E> reader) throws E{
        for(final K key: getKeys())
            reader.read(key, getRecord(key));
    }

    /**
     * Executes reader through records in this set in parallel.
     * <p>
     *     The default implementation doesn't support record set partitioning. This means
     *     that the each record will be processed in the separated task (submitted via {@link java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)})
     *     that may be not acceptable for huge record sets with hundred of elements.
     * @param reader The record reader.
     * @param executor An executor used to schedule readers.
     * @param <E> Type of the exception that may be thrown by reader.
     * @throws E An exception occurred in the reader.
     */
    @Override
    protected <E extends Exception> void forEachParallel(final RecordReader<? super K, ? super V, E> reader,
                                         final ExecutorService executor) throws E {
        try {
            forEachParallel(reader, executor, new Function<WriteOnceRef<Exception>, Collection<RecordProcessingTask<K, V, E>>>() {
                @Override
                public Collection<RecordProcessingTask<K, V, E>> apply(final WriteOnceRef<Exception> errorHandler) {
                    final Collection<RecordProcessingTask<K, V, E>> tasks = new ArrayList<>(size());
                    for (final K key : getKeys())
                        tasks.add(new RecordProcessingTask<K, V, E>(reader, errorHandler) {
                            @Override
                            protected K getIndex() {
                                return key;
                            }

                            @Override
                            protected V getRecord() {
                                return KeyedRecordSet.this.getRecord(key);
                            }
                        });
                    return tasks;
                }
            });
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
     * Gets the size of this record set.
     *
     * @return The size of this record set.
     */
    @Override
    public final int size() {
        return getKeys().size();
    }

}
