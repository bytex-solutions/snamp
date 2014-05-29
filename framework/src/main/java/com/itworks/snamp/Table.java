package com.itworks.snamp;

import com.itworks.snamp.internal.semantics.ThreadSafe;

import java.util.*;

/**
 * Represents generic in-memory table.
 * @param <COLUMN> Type of the column descriptor. It is recommended to use {@link String} as a title for table columns.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see SimpleTable
 */
public interface Table<COLUMN> {
    /**
     * Returns a set of available columns.
     * @return A set of available columns.
     */
    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe(false)
    Set<COLUMN> getColumns();

    /**
     * Returns the count of rows in this table.
     * @return The count of rows in this table.
     */
    @ThreadSafe(false)
    int getRowCount();

    /**
     * Returns a cell value.
     * @param column The cell column.
     * @param row The cell row (zero-based).
     * @return The cell value.
     * @throws IndexOutOfBoundsException The cell doesn't exist.
     */
    @ThreadSafe(false)
    Object getCell(final COLUMN column, final int row) throws IndexOutOfBoundsException;

    /**
     * Returns the type of values at the specified column.
     * @param column The column ID.
     * @return The type of values at the specified column.
     * @throws IndexOutOfBoundsException  The specified column doesn't exist.
     */
    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe(false)
    Class<?> getColumnType(final COLUMN column) throws IndexOutOfBoundsException;

    /**
     * Sets cell value.
     * @param column The column identifier.
     * @param row The row number (zero-based).
     * @param value The cell value to set.
     * @throws UnsupportedOperationException Operation is not supported because this table is read-only.
     * @throws ClassCastException The value type is not compliant with column type.
     */
    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe(false)
    void setCell(final COLUMN column, final int row, final Object value) throws UnsupportedOperationException, ClassCastException;

    /**
     * Adds a new row to the end of the table.
     * @param values The values of the row.
     * @throws UnsupportedOperationException Operation is not supported because this table is read-only.
     * @throws ClassCastException The value type is not compliant with column type.
     * @throws IllegalArgumentException The count of values doesn't match to column count.
     */
    @ThreadSafe(false)
    void addRow(final Map<COLUMN, Object> values) throws UnsupportedOperationException, ClassCastException, IllegalArgumentException;

    /**
     * Removes the row from this table.
     * @param rowIndex An index of the row to remove.
     * @throws UnsupportedOperationException Operation is not supported because this table is read-only.
     */
    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe(false)
    void removeRow(final int rowIndex) throws UnsupportedOperationException;
}