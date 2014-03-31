package com.snamp;

import java.util.*;

/**
 * Represents default implementation of the in-memory table.
 * @param <COLUMN> Type of the column descriptor. It is recommended to use {@link String}.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class SimpleTable<COLUMN> extends ArrayList<Map<COLUMN, Object>> implements Table<COLUMN> {
    private final Map<COLUMN, Class<?>> _columns;

    /**
     * Initializes a new simple table with the specified set of columns.
     * @param cols An array of unique columns.
     */
    public SimpleTable(final Map.Entry<COLUMN, Class<?>>... cols){
        _columns = new HashMap<>(cols.length);
        for(final Map.Entry<COLUMN, Class<?>> c: cols)
            _columns.put(c.getKey(), c.getValue());
    }

    /**
     * Initializes a new simple table with the specified collection of columns.
     * @param columns A collection of unique columns.
     */
    public SimpleTable(final Map<COLUMN, Class<?>> columns){
        _columns = new HashMap<>(columns);
    }

    /**
     * Creates a simple table from an array of rows.
     * @param rows An array of rows.
     * @param <COLUMN> Type of the column descriptor.
     * @return A new instance of the in-memory table.
     */
    public final static <COLUMN> SimpleTable<COLUMN> fromArray(final Map<COLUMN, Object>[] rows){
        if(rows.length == 0) return new SimpleTable<COLUMN>();
        SimpleTable<COLUMN> result = null;
        for(final Map<COLUMN, Object> row: rows){
            if(result == null){
                final Map<COLUMN, Class<?>> columns = new HashMap<>(10);
                for(final COLUMN key: row.keySet())
                    columns.put(key, Object.class);
                result = new SimpleTable<COLUMN>(columns);
            }
            result.addRow(row);
        }
        return result;
    }

    /**
     * Converts this table to the array of rows.
     * @return An array of rows.
     */
    public final Map<COLUMN, Object>[] toArray(){
        final Map<COLUMN, Object>[] rows = new HashMap[size()];
        for(int i = 0; i < size(); i++)
            rows[i] = new HashMap<>(this.get(i));
        return rows;
    }

    /**
     * Returns a set of available columns.
     *
     * @return A set of available columns.
     */
    @Override
    public final Set<COLUMN> getColumns() {
        return _columns.keySet();
    }

    /**
     * Returns a count of rows.
     *
     * @return The count of rows in this table.
     */
    @Override
    public final int getRowCount() {
        return size();
    }

    /**
     * Returns a cell value.
     *
     * @param column The cell column.
     * @param rowIndex    The cell row (zero-based).
     * @return The cell value.
     * @throws IndexOutOfBoundsException The cell doesn't exist.
     */
    @Override
    public final Object getCell(final COLUMN column, final int rowIndex) throws IndexOutOfBoundsException {
        final Map<COLUMN, Object> row = get(rowIndex);
        if(row == null) throw new IndexOutOfBoundsException(String.format("Row %s doesn't exist", rowIndex));
        else if(row.containsKey(column)) return row.get(column);
        else throw new IndexOutOfBoundsException(String.format("Column %s doesn't exist", column));
    }

    /**
     * Returns the type of values at the specified column.
     *
     * @param column The column ID.
     * @return The type of values at the specified column.
     * @throws IndexOutOfBoundsException The specified column doesn't exist.
     */
    @Override
    public final Class<?> getColumnType(final COLUMN column) throws IndexOutOfBoundsException {
        if(_columns.containsKey(column)) return _columns.get(column);
        else throw new IndexOutOfBoundsException(String.format("Column %s doesn't exist.", column));
    }

    /**
     * Sets cell value.
     *
     * @param column The column identifier.
     * @param rowIndex    The row number (zero-based).
     * @param value  The cell value to set.
     * @throws ClassCastException            The value type is not compliant with column type.
     */
    @Override
    public final void setCell(final COLUMN column, final int rowIndex, final Object value) throws ClassCastException {
        final Class<?> columnType = getColumnType(column);
        final Map<COLUMN, Object> row = get(rowIndex);
        if(row == null) return;
        else row.put(column, columnType.cast(value));
    }

    /**
     * Adds a new row to the end of the table.
     *
     * @param values The values of the row.
     * @throws IllegalArgumentException      The count of values doesn't match to column count.
     */
    @Override
    public final void addRow(final Map<COLUMN, Object> values) throws IllegalArgumentException {
        if(values.size() < _columns.size()) throw new IllegalArgumentException(String.format("Expected %s values", _columns.size()));
        add(new HashMap<COLUMN, Object>(values));
    }

    /**
     * Removes the row from this table.
     *
     * @param rowIndex An index of the row to remove.
     */
    @Override
    public final void removeRow(final int rowIndex) {
        remove(rowIndex);
    }
}
