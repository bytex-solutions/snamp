package com.itworks.snamp.connectors;

import com.itworks.snamp.mapping.RecordReader;
import com.itworks.snamp.mapping.RecordSet;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents table reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TableReader<E extends Exception> implements RecordReader<Integer, RecordSet<String, Object>, E> {
    private final ManagedEntityTabularType tabularType;

    private final static class CellSet implements RecordSet<String, ManagedEntityValue<?>>{
        private final ManagedEntityTabularType tabularType;
        private final RecordSet<String, Object> recordSet;

        private CellSet(final ManagedEntityTabularType tabularType,
                        final RecordSet<String, Object> underlyingRecordSet){
            this.tabularType = tabularType;
            this.recordSet = underlyingRecordSet;
        }

        @Override
        public <E extends Exception> void forEach(final RecordReader<? super String, ? super ManagedEntityValue<?>, E> reader) throws E {
            recordSet.forEach(new RecordReader<String, Object, E>() {
                @Override
                public void read(final String columnName, final Object cellValue) throws E {
                    reader.read(columnName, new ManagedEntityValue<>(cellValue, tabularType.getColumnType(columnName)));
                }
            });
        }

        @Override
        public int size() {
            return recordSet.size();
        }

        @Override
        public CellSet parallel(final ExecutorService executor) {
            return new CellSet(tabularType, recordSet.parallel(executor));
        }

        @Override
        public CellSet sequential() {
            return new CellSet(tabularType, recordSet.sequential());
        }
    }

    protected TableReader(final ManagedEntityTabularType tabularType){
        this.tabularType = Objects.requireNonNull(tabularType);
    }

    /**
     * Processes a single row.
     *
     * @param rowIndex An index of the row.
     * @param row A row content.
     * @throws E Unable to process row.
     */
    @Override
    public final void read(final Integer rowIndex, final RecordSet<String, Object> row) throws E {
        read(rowIndex, new CellSet(tabularType, row));
    }

    /**
     * Processes a single row.
     * @param rowIndex A row index.
     * @param row A row content.
     * @throws E Unable to process row.
     */
    protected abstract void read(final int rowIndex,
                                 final RecordSet<String, ManagedEntityValue<?>> row) throws E;
}
