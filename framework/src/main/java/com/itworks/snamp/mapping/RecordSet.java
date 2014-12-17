package com.itworks.snamp.mapping;

import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.views.ParallelView;
import com.itworks.snamp.views.SequentialView;
import com.itworks.snamp.views.ViewSpecific;

/**
 * Represents a set of records.
 * <p>
 *     Record set is a immutable and thread-safe object that allows to read records.
 *     You cannot add or remove elements from the record set.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
public interface RecordSet<I, R> extends SequentialView<RecordSet<I, R>>, ParallelView<RecordSet<I, R>> {
    /**
     * Executes reader through all records in this set.
     * <p>
     *     This method may enumerate all records in parallel or sequential manner.
     *     Use {@link #sequential()} or {@link #parallel(java.util.concurrent.ExecutorService)} to ensure the enumeration behavior.
     * @param <E> Type of the exception that may be thrown by reader.
     * @param reader An object that accepts the record. Cannot be {@literal null}.
     * @throws E Unable to process record.
     */
    @ViewSpecific({SequentialView.class, ParallelView.class})
    <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E;

    /**
     * Gets the size of this record set.
     * @return The size of this record set.
     */
    int size();
}
