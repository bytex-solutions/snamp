package com.itworks.snamp.mapping;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RowSetWithInsertedRow<C> implements RowSet<C> {
    private final int insertedRowIndex;
    private final RecordSet<String, C> insertedRow;
    private final RowSet<C> source;

    RowSetWithInsertedRow(final RowSet<C> source,
                         final RecordSet<String, C> insertedRow,
                         final int index){
        this.source = source;
        this.insertedRowIndex = index;
        this.insertedRow = insertedRow;
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
        if (insertedRowIndex == source.size()) {
            source.forEach(reader);
            reader.read(insertedRowIndex, insertedRow);
        } else source.forEach(new RecordReader<Integer, RecordSet<String, C>, E>() {
            @Override
            public void read(final Integer index, final RecordSet<String, C> value) throws E {
                if (index < insertedRowIndex)
                    reader.read(index, value);
                else if (index == insertedRowIndex) {
                    reader.read(index, insertedRow);
                    reader.read(index + 1, value);
                } else reader.read(index + 1, value);
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
        return source.size() + 1;
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
    public RowSetWithInsertedRow<C> parallel(final ExecutorService executor) {
        return new RowSetWithInsertedRow<>(source.parallel(executor), insertedRow, insertedRowIndex);
    }

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    public RowSetWithInsertedRow<C> sequential() {
        return new RowSetWithInsertedRow<>(source.sequential(), insertedRow, insertedRowIndex);
    }
}
