package com.itworks.snamp.mapping;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RowSetWithDeletedRow<C> implements RowSet<C> {
    private final int deletedRow;
    private final RowSet<C> source;

    RowSetWithDeletedRow(final RowSet<C> source,
                         final int deletedRow) {
        this.source = source;
        this.deletedRow = deletedRow;
    }

    @Override
    public int size() {
        return source.size() - 1;
    }

    @Override
    public Set<String> getColumns() {
        return source.getColumns();
    }

    @Override
    public boolean isIndexed(final String columnName) {
        return source.isIndexed(columnName);
    }

    @Override
    public <E extends Exception> void forEach(final RecordReader<? super Integer, ? super RecordSet<String, C>, E> reader) throws E {
        source.forEach(new FilteredRowReader<Integer, RecordSet<String, C>, E>() {
            @Override
            protected boolean isAllowed(final Integer index) {
                return index != deletedRow;
            }

            @Override
            protected void readCore(final Integer index, final RecordSet<String, C> value) throws E {
                reader.read(index < deletedRow ? index : index - 1, value);
            }
        });
    }

    @Override
    public RowSetWithDeletedRow<C> parallel(final ExecutorService executor) {
        return new RowSetWithDeletedRow<>(source.parallel(executor), deletedRow);
    }

    @Override
    public RowSetWithDeletedRow<C> sequential() {
        return new RowSetWithDeletedRow<>(source.sequential(), deletedRow);
    }
}
