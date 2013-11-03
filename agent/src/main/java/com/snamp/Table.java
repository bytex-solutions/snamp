package com.snamp;

import java.util.*;

/**
 * Represents generic table.
 * @author roman
 */
public interface Table<COLUMN> {
    /**
     * Returns a set of available columns.
     * @return A set of available columns.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Set<COLUMN> getColumns();

    /**
     * Returns a count of rows.
     * @return
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public int getRowCount();

    /**
     * Returns a cell value.
     * @param column The cell column.
     * @param row The cell row (zero-based).
     * @return The cell value.
     * @throws IndexOutOfBoundsException The cell doesn't exist.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Object getCell(final COLUMN column, final int row) throws IndexOutOfBoundsException;

    /**
     * Returns the type of values at the specified column.
     * @param column The column ID.
     * @return The type of values at the specified column.
     * @throws IndexOutOfBoundsException  The specified column doesn't exist.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Class<?> getColumnType(final COLUMN column) throws IndexOutOfBoundsException;

    /**
     * Sets cell value.
     * @param column The column identifier.
     * @param row The row number (zero-based).
     * @param value The cell value to set.
     * @throws UnsupportedOperationException Operation is not supported.
     * @throws ClassCastException The value type is not compliant with column type.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void setCell(final COLUMN column, final int row, final Object value) throws UnsupportedOperationException, ClassCastException;

    /**
     * Adds a new row to the end of the table.
     * @param values The values of the row.
     * @throws UnsupportedOperationException Operation is not supported.
     * @throws ClassCastException The value type is not compliant with column type.
     * @throws IllegalArgumentException The count of values doesn't match to column count.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void addRow(final Map<COLUMN, Object> values) throws UnsupportedOperationException, ClassCastException, IllegalArgumentException;

    /**
     * Removes the row from this table.
     * @param rowIndex An index of the row to remove.
     * @throws UnsupportedOperationException Operation is not supported.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void removeRow(final int rowIndex) throws UnsupportedOperationException;
}
