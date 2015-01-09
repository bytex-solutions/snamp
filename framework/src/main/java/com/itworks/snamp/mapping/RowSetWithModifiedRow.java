package com.itworks.snamp.mapping;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RowSetWithModifiedRow<C> implements RowSet<C> {
    private final RowSet<C> source;
    private final int updatedRowIndex;
    private final RecordSet<String, C> updatedRow;

    RowSetWithModifiedRow(final RowSet<C> source,
                          final RecordSet<String, C> updatedRow,
                          final int updatedRowIndex){
        this.source = source;
        this.updatedRow = updatedRow;
        this.updatedRowIndex = updatedRowIndex;
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
    public <E extends Exception> void forEach(final RecordReader<? super Integer, ? super RecordSet<String, C>, E> reader) throws E {
        source.forEach(new RecordReader<Integer, RecordSet<String, C>, E>() {
            @Override
            public void read(final Integer index, final RecordSet<String, C> value) throws E {
                reader.read(index, index == updatedRowIndex ? updatedRow : value);
            }
        });
    }

    /**
     * Gets the number of rows.
     *
     * @return The number of rows.
     */
    @Override
    public int size() {
        return source.size();
    }

    /**
     * Gets a set of table columns.
     *
     * @return A set of table columns.
     */
    @Override
    public Set<String> getColumns() {
        return source.getColumns();
    }

    /**
     * Determines whether the specified column is indexed.
     *
     * @param columnName The column name.
     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
     */
    @Override
    public boolean isIndexed(final String columnName) {
        return source.isIndexed(columnName);
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
    public RowSetWithModifiedRow<C> parallel(final ExecutorService executor) {
        return new RowSetWithModifiedRow<>(source.parallel(executor), updatedRow, updatedRowIndex);
    }

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    public RowSetWithModifiedRow<C> sequential() {
        return new RowSetWithModifiedRow<>(source.sequential(), updatedRow, updatedRowIndex);
    }
}
