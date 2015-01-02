package com.itworks.snamp.mapping;

import com.google.common.base.Function;
import com.itworks.snamp.concurrent.WriteOnceRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Represents an abstract record set in which index is an ordinal.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class OrdinalRecordSet<I, R> extends AbstractRecordSet<I, R> {

    /**
     * Gets index of the first element.
     * @return Index of the first element; or {@literal null}, if record set is empty.
     */
    protected abstract I first();

    /**
     * Computes index of the next record.
     * @param index Index of the previous record.
     * @return Index of the next record; or {@link null}, if the specified index represents the last record.
     */
    protected abstract I next(final I index);

    /**
     * Gets record at the specified index.
     * @param index The index of the record.
     * @return A record at the specified index.
     */
    protected abstract R getRecord(final I index);

    /**
     * Executes reader sequentially through records in this set.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader The record reader.
     * @throws E An exception occurred in the reader.
     */
    @Override
    protected final <E extends Exception> void forEachSequential(final RecordReader<? super I, ? super R, E> reader) throws E{
        for(I index = first(); index != null; index = next(index))
            reader.read(index, getRecord(index));
    }

    /**
     * Executes reader through records in this set in parallel.
     * <p>
     *     The default implementation doesn't support record set partitioning. This means
     *     that the each record will be processed in the separated task (submitted via {@link java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)})
     *     that may be not acceptable for huge record sets with hundred of elements.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader The record reader.
     * @param executor An executor used to schedule readers.
     * @throws E An exception occurred in the reader.
     */
    @Override
    protected <E extends Exception> void forEachParallel(final RecordReader<? super I, ? super R, E> reader,
                                         final ExecutorService executor) throws E {
        try {
            forEachParallel(reader, executor, new Function<WriteOnceRef<Exception>, Collection<RecordProcessingTask<I, R, E>>>() {
                @Override
                public Collection<RecordProcessingTask<I, R, E>> apply(final WriteOnceRef<Exception> errorHandler) {
                    final Collection<RecordProcessingTask<I, R, E>> tasks = new ArrayList<>(size());
                    for (I index = first(); index != null; index = next(index)) {
                        final I currentIndex = index;
                        tasks.add(new RecordProcessingTask<I, R, E>(reader, errorHandler) {
                            @Override
                            protected I getIndex() {
                                return currentIndex;
                            }

                            @Override
                            protected R getRecord() {
                                return OrdinalRecordSet.this.getRecord(currentIndex);
                            }
                        });
                    }
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
     * @param e Interruption exception.
     */
    protected void forEachInterrupted(final InterruptedException e){

    }
}
