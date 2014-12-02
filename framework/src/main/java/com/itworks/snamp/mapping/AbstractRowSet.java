package com.itworks.snamp.mapping;

import java.util.Set;

/**
 * Represents an abstract class for constructing row sets.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractRowSet<C> extends OrdinalRecordSet<Integer, RecordSet<String, C>> implements RowSet<C> {
    /**
     * Gets index of the first element.
     *
     * @return Index of the first element; or {@literal null}, if record set is empty.
     */
    @Override
    protected final Integer first() {
        return 0;
    }

    /**
     * Computes index of the next record.
     *
     * @param rowIndex Index of the previous record.
     * @return Index of the next record; or {@link null}, if the specified index represents the last record.
     */
    @Override
    protected final Integer next(final Integer rowIndex) {
        return rowIndex < size() - 1 ? rowIndex + 1 : null;
    }

    protected abstract C getCell(final String columnName, final int rowIndex);

    /**
     * Gets record at the specified index.
     *
     * @param rowIndex The index of the record.
     * @return A record at the specified index.
     */
    @Override
    protected final KeyedRecordSet<String, C> getRecord(final Integer rowIndex) {
        return new KeyedRecordSet<String, C>() {
            @Override
            protected Set<String> getKeys() {
                return AbstractRowSet.this.getColumns();
            }

            @Override
            protected C getRecord(final String columnName) {
                return getCell(columnName, rowIndex);
            }
        };
    }
}
