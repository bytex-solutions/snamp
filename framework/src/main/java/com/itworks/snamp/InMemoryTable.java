package com.itworks.snamp;


import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.internal.Utils;

import java.util.*;

/**
 * Represents default implementation of the in-memory table.
 * @param <COLUMN> Type of the column descriptor. It is recommended to use {@link String}.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see com.itworks.snamp.TableFactory
 */
public class InMemoryTable<COLUMN> extends ArrayList<Map<COLUMN, Object>> implements Table<COLUMN> {
    private final ImmutableMap<COLUMN, Class<?>> _columns;

    /**
     * Initializes a new empty table with specified columns and row capacity.
     * @param columns The columns of the table. Cannot be {@literal null}.
     * @param rowCapacity The initial capacity of the table.
     */
    public InMemoryTable(final ImmutableMap<COLUMN, Class<?>> columns,
                            final int rowCapacity){
        super(rowCapacity);
        this._columns = Objects.requireNonNull(columns, "columns is null.");
    }

    /**
     * Gets number of columns in this table.
     * @return The number of columns in this table.
     */
    public final int getColumnCount(){
        return _columns.size();
    }

    /**
     * Converts this table to the array of rows.
     * @return An array of rows.
     */
    @SuppressWarnings("NullableProblems")
    public final Map<COLUMN, Object>[] toArray(){
        @SuppressWarnings("unchecked")
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

    private static IndexOutOfBoundsException createInvalidColumnException(final Object column){
        return new IndexOutOfBoundsException(String.format("Column %s doesn't exist", column));
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
        if(row.containsKey(column)) return row.get(column);
        else throw createInvalidColumnException(column);
    }

    /**
     * Returns a table cell.
     * @param column The cell column.
     * @param rowIndex The cell row (zero-based).
     * @param cellType The expected cell type.
     * @param safeCast {@literal true} for {@literal null} when cell value is not a {@code cellType}; {@literal false} for {@link java.lang.ClassCastException}.
     * @param <CELL> Expected type of the cell.
     * @return The table cell.
     * @throws IndexOutOfBoundsException Incorrect row index or column.
     */
    public final <CELL> com.google.common.collect.Table.Cell<Integer, COLUMN, CELL> getCell(final COLUMN column,
                                                                                            final int rowIndex,
                                                                                            final Class<CELL> cellType,
                                                                                            final boolean safeCast) throws IndexOutOfBoundsException {
        final Map<COLUMN, Object> row = get(rowIndex);
        return new com.google.common.collect.Table.Cell<Integer, COLUMN, CELL>() {
            @Override
            public Integer getRowKey() {
                return rowIndex;
            }

            @Override
            public COLUMN getColumnKey() {
                return column;
            }

            @Override
            public CELL getValue() {
                final Object cellValue = row.get(getColumnKey());
                return safeCast ? Utils.safeCast(cellValue, cellType) : cellType.cast(cellValue);
            }
        };
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
        else throw createInvalidColumnException(column);
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
        if(row != null) row.put(column, columnType.cast(value));
    }

    /**
     * Adds a new row to the end of the table.
     *
     * @param values The values of the row.
     * @throws IllegalArgumentException      The count of values doesn't match to column count.
     */
    @Override
    public final void addRow(final Map<? extends COLUMN, ?> values) throws IllegalArgumentException {
        if(values.size() < _columns.size()) throw new IllegalArgumentException(String.format("Expected %s values", _columns.size()));
        super.add(new HashMap<>(values));
    }

    /**
     * Inserts the row into this table.
     *
     * @param index  Insertion position.
     * @param values The row to insert.
     * @throws UnsupportedOperationException Operation is not supported because this table is read-only.
     * @throws ClassCastException            The value type is not compliant with column type.
     * @throws IllegalArgumentException      The count of values doesn't match to column count.
     */
    @Override
    public final void insertRow(final int index, final Map<? extends COLUMN, ?> values) throws UnsupportedOperationException, ClassCastException, IllegalArgumentException {
        if (values.size() < _columns.size())
            throw new IllegalArgumentException(String.format("Expected %s values", _columns.size()));
        else super.add(index, new HashMap<>(values));
    }

    /**
     * Gets row by its index.
     *
     * @param index Zero-based index of the row.
     * @return The map that represents the row.
     * @throws IndexOutOfBoundsException The specified row doesn't exist.
     */
    @Override
    public final Map<COLUMN, Object> getRow(final int index) {
        return get(index);
    }

    public final <CELL> void getRow(final int index,
                             final Map<COLUMN, CELL> output,
                             final Class<CELL> cellType,
                             final boolean safeCast){
        getTypedRow(this, index, output, cellType, safeCast);
    }

    public static <COLUMN, CELL> void getTypedRow(final Table<COLUMN> table,
                                                          final int index,
                                                          final Map<COLUMN, CELL> output,
                                                          final Class<CELL> cellType,
                                                          final boolean safeCast){
        final Map<COLUMN, Object> source = table.getRow(index);
        for(final Map.Entry<COLUMN, Object> row: source.entrySet())
            output.put(row.getKey(), safeCast ? Utils.safeCast(row.getValue(), cellType) : cellType.cast(row.getValue()));
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

    /**
     * Creates (but not add) a new row.
     * @return A new instance of the row.
     */
    public final Map<COLUMN, Object> newRow(){
        return new HashMap<>(_columns.size());
    }


    /**
     * Returns a list of columns that saves the order.
     * @param table The table value.
     * @param <C> Type of the table columns.
     * @return A list of ordered columns.
     */
    public static <C> List<C> getOrderedColumns(final Table<C> table){
        return table != null ? new ArrayList<>(table.getColumns()) : null;
    }

    /**
     * Returns an ordered list of column values at the specific row.
     * @param table The table value.
     * @param columns The ordered collection of columns.
     * @param rowIndex The row number (zero-based).
     * @param <C> Type of the table columns.
     * @return A new ordered collection of column values at the specific row.
     */
    public static <C> List<?> getRow(final Table<C> table,
                                     final List<C> columns,
                                     final int rowIndex) {
        if(table == null || columns == null || columns.isEmpty())
            return Collections.emptyList();
        final List<Object> result = new ArrayList<>(columns.size());
        for (int i = 0; i < columns.size(); i++)
            result.add(i, table.getCell(columns.get(i), rowIndex));
        return result;
    }

    /**
     * Updates the whole row.
     *
     * @param index Zero-based index of the row.
     * @param row   A new row.
     * @throws UnsupportedOperationException Operation is not supported because this table is read-only.
     * @throws ClassCastException            The value type is not compliant with column type.
     * @throws IllegalArgumentException      The count of values doesn't match to column count.
     */
    @Override
    public void setRow(final int index, final Map<? extends COLUMN, Object> row) throws UnsupportedOperationException, ClassCastException, IllegalArgumentException {
        if(row == null) throw new IllegalArgumentException("row is null.");
        else if(row.size() < _columns.size()) throw new IllegalArgumentException("Row is not well formed");
        else super.get(index).putAll(row);
    }
}
