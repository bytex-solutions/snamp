package com.itworks.snamp.mapping;

import com.itworks.snamp.WriteOnceRef;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Represents an abstract record set in which index is an ordinal.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class OrdinalRecordSet<I, R> implements RecordSet<I, R> {
    private static abstract class ProxyOrdinalRecordSet<I, R> extends OrdinalRecordSet<I, R>{
        protected final OrdinalRecordSet<I, R> recordSet;

        private ProxyOrdinalRecordSet(final OrdinalRecordSet<I, R> os){
            recordSet = os;
        }

        @Override
        protected final I first() {
            return recordSet.first();
        }

        @Override
        protected final I next(final I index) {
            return recordSet.next(index);
        }

        @Override
        protected final R getRecord(final I index) {
            return recordSet.getRecord(index);
        }

        @Override
        public final OrdinalRecordSet<I, R> parallel(final ExecutorService executor) {
            return recordSet.parallel(executor);
        }

        @Override
        public final OrdinalRecordSet<I, R> sequential() {
            return recordSet.sequential();
        }

        @Override
        public final int size() {
            return recordSet.size();
        }

        @Override
        protected final void forEachInterrupted(final InterruptedException e) {
            recordSet.forEachInterrupted(e);
        }

        @Override
        public abstract <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E;
    }

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
    protected final <E extends Exception> void forEachSequential(final RecordReader<? super I, ? super R, E> reader) throws E{
        for(I index = first(); index != null; index = next(index))
            reader.read(index, getRecord(index));
    }

    /**
     * Executes reader through records in this set in parallel.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader The record reader.
     * @param executor An executor used to schedule readers.
     * @throws E An exception occurred in the reader.
     */
    @SuppressWarnings("unchecked")
    protected final <E extends Exception> void forEachParallel(final RecordReader<? super I, ? super R, E> reader,
                                         final ExecutorService executor) throws E {
        final List<Callable<Void>> tasks = new ArrayList<>(size());
        final WriteOnceRef<Exception> fault = new WriteOnceRef<>(null);
        for (I index = first(); index != null; index = next(index)) {
            final I currentIndex = index;
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        reader.read(currentIndex, getRecord(currentIndex));
                    }
                    catch (final Exception e){
                        fault.set(e);
                        throw e;
                    }
                    return null;
                }
            });
        }
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
     * @param e Interruption exception.
     */
    protected void forEachInterrupted(final InterruptedException e){

    }

    /**
     * Iterates over each record in this set.
     * <p>
     *     In the default implementation this method calls {@link #forEachSequential(RecordReader)}.
     * @param reader An object that accepts the record.
     * @throws E Unable to read records.
     * @see #forEachParallel(RecordReader, java.util.concurrent.ExecutorService)
     * @see #forEachSequential(RecordReader)
     */
    @Override
    public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E{
        forEachSequential(reader);
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
    public OrdinalRecordSet<I, R> parallel(final ExecutorService executor){
        return parallel(this, executor);
    }

    private static <I, R> ProxyOrdinalRecordSet<I, R> parallel(final OrdinalRecordSet<I, R> parent,
                                                               final ExecutorService executor){
        return new ProxyOrdinalRecordSet<I, R>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {
                recordSet.forEachParallel(reader, executor);
            }
        };
    }

    private static <I, R> ProxyOrdinalRecordSet<I, R> sequential(final OrdinalRecordSet<I, R> parent){
        return new ProxyOrdinalRecordSet<I, R>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {
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
    public OrdinalRecordSet<I, R> sequential(){
        return sequential(this);
    }
}
