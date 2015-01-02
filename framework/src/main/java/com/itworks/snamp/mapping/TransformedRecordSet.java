package com.itworks.snamp.mapping;

import com.google.common.base.Function;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TransformedRecordSet<I1, I2, V1, V2> implements RecordSet<I2, V2> {
    private final Function<V1, V2> recordTransformation;
    private final Function<I1, I2> indexTranformation;
    private final RecordSet<I1, V1> source;

    TransformedRecordSet(final RecordSet<I1, V1> source,
                         final Function<I1, I2> indexTransf,
                         final Function<V1, V2> recordTransf){
        this.recordTransformation = Objects.requireNonNull(recordTransf);
        this.indexTranformation = Objects.requireNonNull(indexTransf);
        this.source = source;
    }

    /**
     * Executes reader through all records in this set.
     * <p/>
     * This method may enumerate all records in parallel or sequential manner.
     * Use {@link #sequential()} or {@link #parallel(java.util.concurrent.ExecutorService)} to ensure the enumeration behavior.
     *
     * @param reader An object that accepts the record. Cannot be {@literal null}.
     * @throws E Unable to process record.
     */
    @Override
    public <E extends Exception> void forEach(final RecordReader<? super I2, ? super V2, E> reader) throws E {
        source.forEach(new RecordReader<I1, V1, E>() {
            @Override
            public void read(final I1 index, final V1 value) throws E {
                reader.read(indexTranformation.apply(index), recordTransformation.apply(value));
            }
        });
    }

    /**
     * Gets the size of this record set.
     *
     * @return The size of this record set.
     */
    @Override
    public int size() {
        return source.size();
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
    public RecordSet<I2, V2> parallel(final ExecutorService executor) {
        return new TransformedRecordSet<>(source.parallel(executor), indexTranformation, recordTransformation);
    }

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    public RecordSet<I2, V2> sequential() {
        return new TransformedRecordSet<>(source.sequential(), indexTranformation, recordTransformation);
    }
}
