package com.itworks.snamp.mapping;

import com.google.common.base.Function;
import com.itworks.snamp.concurrent.WriteOnceRef;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Represents an abstract class for constructing custom record sets.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractRecordSet<I, R> implements RecordSet<I, R> {

    private static abstract class ProxyRecordSet<I, R> implements RecordSet<I, R>{
        protected final AbstractRecordSet<I, R> recordSet;

        private ProxyRecordSet(final AbstractRecordSet<I, R> rs){
            recordSet = Objects.requireNonNull(rs);
        }

        @Override
        public final int size() {
            return recordSet.size();
        }

        @Override
        public final RecordSet<I, R> parallel(final ExecutorService executor) {
            return recordSet.parallel(executor);
        }

        @Override
        public final RecordSet<I, R> sequential() {
            return recordSet.sequential();
        }
    }

    /**
     * Represents record processing task which will be executed asynchronously.
     * @param <I> Type of the record index.
     * @param <R> Type of the record content.
     * @param <E> Type of the exception that can be produced by reader.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    static abstract class RecordProcessingTask<I, R, E extends Exception> implements Callable<Void>{
        private final RecordReader<? super I, ? super R, E> reader;
        private final WriteOnceRef<Exception> errorHolder;

        protected RecordProcessingTask(final RecordReader<? super I, ? super R, E> reader,
                                       final WriteOnceRef<Exception> errorHolder){
            this.reader = Objects.requireNonNull(reader);
            this.errorHolder = Objects.requireNonNull(errorHolder);
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws E if unable to compute a result
         */
        @Override
        public final Void call() throws Exception {
            try {
                read();
            }
            catch (final Exception e){
                errorHolder.set(e);
                throw e;
            }
            return null;
        }

        public final void read() throws E{
            reader.read(getIndex(), getRecord());
        }

        /**
         * Gets an index of the record to be processed asynchronously.
         * @return An index of the record to be processed asynchronously.
         */
        protected abstract I getIndex();

        /**
         * Gets a record to be processed asynchronously.
         * @return A record to be processed asynchronously.
         */
        protected abstract R getRecord();
    }

    /**
     * Executes reader sequentially through records in this set.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader The record reader.
     * @throws E An exception occurred in the reader.
     */
    protected abstract <E extends Exception> void forEachSequential(final RecordReader<? super I, ? super R, E> reader) throws E;

    /**
     * Executes reader through records in this set in parallel.
     * @param reader The record reader.
     * @param executor An executor used to schedule readers.
     * @param <E> Type of the exception that may be thrown by reader.
     * @throws E An exception occurred in the reader.
     */
    protected abstract <E extends Exception> void forEachParallel(final RecordReader<? super I, ? super R, E> reader,
                                                                  final ExecutorService executor) throws E;

    static <I, R, E extends Exception> void forEachParallel(final RecordReader<? super I, ? super R, E> reader,
                                                                      final ExecutorService executor,
                                                                      final Function<WriteOnceRef<Exception>, Collection<RecordProcessingTask<I, R, E>>> taskListFactory) throws E, InterruptedException {
        final WriteOnceRef<Exception> errorHolder = new WriteOnceRef<>(null);
        final Collection<RecordProcessingTask<I, R, E>> tasks = taskListFactory.apply(errorHolder);
        //wait for completion
        executor.invokeAll(tasks != null ? tasks : Collections.<RecordProcessingTask<I, R, E>>emptyList());
        RecordReaderUtils.checkAndThrow(reader, errorHolder.get());
    }

    private static <I, R> ProxyRecordSet<I, R> sequential(final AbstractRecordSet<I, R> parent){
        return new ProxyRecordSet<I, R>(parent){
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {
                recordSet.forEachSequential(reader);
            }
        };
    }

    private static <I, R> ProxyRecordSet<I, R> parallel(final AbstractRecordSet<I, R> parent,
                                   final ExecutorService executor){
        return new ProxyRecordSet<I, R>(parent){
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {
                recordSet.forEachParallel(reader, executor);
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
    public RecordSet<I, R> sequential() {
        return sequential(this);
    }

    /**
     * Executes reader through all records in this set.
     * <p>
     *     By default, this method calls {@link #forEachSequential(RecordReader)} method.
     * @param reader An object that accepts the record. Cannot be {@literal null}.
     * @throws E Unable to process record.
     */
    @Override
    public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {
        forEachSequential(reader);
    }

    /**
     * Returns an equivalent object that is parallel.
     * May return itself, either because the object was already parallel,
     * or because the underlying object state was modified to be parallel.
     *
     * @param executor An executor used to apply methods in parallel manner.
     * @return An object that supports parallel execution of some methods.
     */
    @Override
    public RecordSet<I, R> parallel(final ExecutorService executor) {
        return parallel(this, executor);
    }
}
