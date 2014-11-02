package com.itworks.snamp;


import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.internal.Utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * Represents default implementation of the in-memory table.
 * @param <COLUMN> Type of the column descriptor. It is recommended to use {@link String}.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class InMemoryTable<COLUMN> extends ArrayList<Map<COLUMN, Object>> implements Table<COLUMN> {
    private final ImmutableMap<COLUMN, Class<?>> _columns;

    /**
     * Initializes a new table using the column builder.
     * <p>
     *     Note that the builder will be cleaned inside of the constructor.
     * @param builder The column builder with constructed columns.
     * @param rowCapacity The initial capacity of the table.
     */
    public <E extends Exception> InMemoryTable(final Consumer<ImmutableMap.Builder<COLUMN, Class<?>>, E> builder,
                                               final int rowCapacity) throws E {
        super(rowCapacity);
        final ImmutableMap.Builder<COLUMN, Class<?>> b = ImmutableMap.builder();
        builder.accept(b);
        _columns = b.build();
    }

    private InMemoryTable(final Iterator<COLUMN> columns,
                          final int columnCount,
                          final Class<?> columnType,
                          final int rowCapacity) {
        super(rowCapacity);
        switch (columnCount) {
            case 0:
                _columns = ImmutableMap.of();
                return;
            case 1:
                _columns = ImmutableMap.<COLUMN, Class<?>>of(columns.next(),
                        columnType);
                return;
            case 2:
                _columns = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType);
                return;
            case 3:
                _columns = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                return;
            case 4:
                _columns = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                return;
            case 5:
                _columns = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                return;
            default:
                final ImmutableMap.Builder<COLUMN, Class<?>> builder = ImmutableMap.builder();
                while (columns.hasNext())
                    builder.put(columns.next(), columnType);
                _columns = builder.build();
        }
    }

    /**
     * Initializes a new table with identical column type.
     * @param columns A collection of columns.
     * @param columnType The type of all columns.
     * @param rowCapacity Expected count of rows.
     */
    public InMemoryTable(final Collection<COLUMN> columns,
                         final Class<?> columnType,
                         final int rowCapacity){
        this(columns.iterator(), columns.size(), columnType, rowCapacity);
    }

    /**
     * Initializes a new table using the specified collection of columns and rows.
     * <p>
     *     This constructor saves passed map with columns as-is without copying the
     *     key/value pairs.
     * @param columns A map for columns.
     * @param rows A map of rows.
     */
    public InMemoryTable(final ImmutableMap<COLUMN, Class<?>> columns,
                         final Collection<Map<COLUMN, Object>> rows) {
        super(rows.size());
        this._columns = Objects.requireNonNull(columns, "columns is null.");
        addAll(rows);
    }

    /**
     * Initializes a new simple table with the specified set of columns.
     * @param cols An array of unique columns.
     */
    @SafeVarargs
    public InMemoryTable(final Map.Entry<COLUMN, Class<?>>... cols) {
        switch (cols.length) {
            case 0:
                _columns = ImmutableMap.of();
                return;
            case 1:
                _columns = createColumns(cols[0]);
                return;
            case 2:
                _columns = createColumns(cols[0], cols[1]);
                return;
            case 3:
                _columns = createColumns(cols[0], cols[1], cols[2]);
                return;
            case 4:
                _columns = createColumns(cols[0], cols[1], cols[2], cols[3]);
                return;
            case 5:
                _columns = createColumns(cols[0], cols[1], cols[2], cols[3], cols[4]);
                return;
            default:
                ImmutableMap.Builder<COLUMN, Class<?>> builder = ImmutableMap.builder();
                for (final Map.Entry<COLUMN, Class<?>> column : cols)
                    builder = builder.put(column);
                _columns = builder.build();
        }
    }

    private static <COLUMN> ImmutableMap<COLUMN, Class<?>> createColumns(final Map.Entry<COLUMN, Class<?>> column1,
                                                                         final Map.Entry<COLUMN, Class<?>> column2,
                                                                         final Map.Entry<COLUMN, Class<?>> column3,
                                                                         final Map.Entry<COLUMN, Class<?>> column4,
                                                                         final Map.Entry<COLUMN, Class<?>> column5){
        return ImmutableMap.of(column1.getKey(), column1.getValue(),
                column2.getKey(), column2.getValue(),
                column3.getKey(), column3.getValue(),
                column4.getKey(), column4.getValue(),
                column5.getKey(), column5.getValue());
    }

    private static <COLUMN> ImmutableMap<COLUMN, Class<?>> createColumns(final Map.Entry<COLUMN, Class<?>> column1,
                                                                         final Map.Entry<COLUMN, Class<?>> column2,
                                                                         final Map.Entry<COLUMN, Class<?>> column3,
                                                                         final Map.Entry<COLUMN, Class<?>> column4){
        return ImmutableMap.of(column1.getKey(), column1.getValue(),
                column2.getKey(), column2.getValue(),
                column3.getKey(), column3.getValue(),
                column4.getKey(), column4.getValue());
    }

    private static <COLUMN> ImmutableMap<COLUMN, Class<?>> createColumns(final Map.Entry<COLUMN, Class<?>> column1,
                                                                         final Map.Entry<COLUMN, Class<?>> column2,
                                                                         final Map.Entry<COLUMN, Class<?>> column3){
        return ImmutableMap.of(column1.getKey(), column1.getValue(),
                column2.getKey(), column2.getValue(),
                column3.getKey(), column3.getValue());
    }

    private static <COLUMN> ImmutableMap<COLUMN, Class<?>> createColumns(final Map.Entry<COLUMN, Class<?>> column1,
                                                                         final Map.Entry<COLUMN, Class<?>> column2){
        return ImmutableMap.of(column1.getKey(), column1.getValue(),
                column2.getKey(), column2.getValue());
    }

    private static <COLUMN> ImmutableMap<COLUMN, Class<?>> createColumns(final Map.Entry<COLUMN, Class<?>> column){
        return ImmutableMap.<COLUMN, Class<?>>of(column.getKey(), column.getValue());
    }

    /**
     * Initializes a new simple table with the specified collection of columns.
     * @param columns A collection of unique columns.
     */
    public InMemoryTable(final Map<COLUMN, Class<?>> columns){
        this(columns, 5);
    }

    /**
     * Initializes a new table with the specified columns and initial row capacity.
     * @param columns A collection of unique columns.
     * @param rowCapacity Initial row capacity.
     */
    public InMemoryTable(final Map<COLUMN, Class<?>> columns, final int rowCapacity) {
        super(rowCapacity);
        _columns = ImmutableMap.copyOf(columns);
    }

    /**
     * Initializes a new table with single column.
     * @param columnId The identifier of the column.
     * @param columnType The column type.
     * @param rowCapacity The initial capacity of the table.
     */
    public InMemoryTable(final COLUMN columnId, final Class<?> columnType, final int rowCapacity) {
        super(rowCapacity);
        _columns = ImmutableMap.<COLUMN, Class<?>>of(columnId, columnType);
    }

    /**
     * Initializes a new table with two columns.
     * @param columnId1 The identifier of the first column.
     * @param columnType1 The type of the first column.
     * @param columnId2 The identifier of the second column.
     * @param columnType2 The type of the second column.
     * @param rowCapacity The initial capacity of the table.
     */
    public InMemoryTable(final COLUMN columnId1, final Class<?> columnType1,
                         final COLUMN columnId2, final Class<?> columnType2,
                         final int rowCapacity){
        super(rowCapacity);
        _columns = ImmutableMap.of(columnId1, columnType1, columnId2, columnType2);
    }

    /**
     * Initializes a new table with three columns.
     * @param columnId1 The identifier of the first column.
     * @param columnType1 The type of the first column.
     * @param columnId2 The identifier of the second column.
     * @param columnType2 The type of the second column.
     * @param columnId3 The identifier of the third column.
     * @param columnType3 The type of the third column.
     * @param rowCapacity The initial capacity of the table.
     */
    public InMemoryTable(final COLUMN columnId1, final Class<?> columnType1,
                         final COLUMN columnId2, final Class<?> columnType2,
                         final COLUMN columnId3, final Class<?> columnType3,
                         final int rowCapacity) {
        super(rowCapacity);
        _columns = ImmutableMap.of(columnId1, columnType1,
                columnId2, columnType2,
                columnId3, columnType3);
    }

    public static <COLUMN> InMemoryTable<COLUMN> fromRow(final Map<COLUMN, ?> row) {
        final InMemoryTable<COLUMN> result = new InMemoryTable<>(new SafeConsumer<ImmutableMap.Builder<COLUMN, Class<?>>>() {
            @Override
            public void accept(final ImmutableMap.Builder<COLUMN, Class<?>> input) {
                for (final Map.Entry<COLUMN, ?> entry : row.entrySet())
                    input.put(entry.getKey(), entry.getValue().getClass());
            }
        },
                1);
        result.addRow(row);
        return result;
    }

    /**
     * Creates a simple table from an array of rows.
     * @param rows An array of rows.
     * @param <COLUMN> Type of the column descriptor.
     * @return A new instance of the in-memory table.
     */
    public static <COLUMN> InMemoryTable<COLUMN> fromArray(final Map<COLUMN, Object>[] rows){
        if(rows.length == 0) return new InMemoryTable<>();
        InMemoryTable<COLUMN> result = null;
        for(final Map<COLUMN, Object> row: rows){
            if(result == null){
                final Map<COLUMN, Class<?>> columns = new HashMap<>(10);
                for(final COLUMN key: row.keySet())
                    columns.put(key, Object.class);
                result = new InMemoryTable<>(columns, rows.length);
            }
            result.addRow(row);
        }
        return result;
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

    private static Table<String> create(final List<PropertyDescriptor> columns,
                                        final Collection<?> rows) throws ReflectiveOperationException {
        final Table<String> result = new InMemoryTable<>(new SafeConsumer<ImmutableMap.Builder<String, Class<?>>>() {
            @Override
            public void accept(final ImmutableMap.Builder<String, Class<?>> builder) {
                for (final PropertyDescriptor descr : columns)
                    builder.put(descr.getName(), descr.getPropertyType());
            }
        }, rows.size());
        for (final Object source : rows) {
            final Map<String, Object> destination = new HashMap<>(columns.size());
            for (final PropertyDescriptor descr : columns)
                destination.put(descr.getName(), descr.getReadMethod().invoke(source));
            result.addRow(destination);
        }
        return result;
    }

    /**
     * Creates a new table from an array of JavaBeans.
     * @param rowType The type of the JavaBean.
     * @param rows A collection of objects.
     * @param columns An set of JavaBean property names to be used as columns.
     * @param <T> Type of the row.
     * @return A new instance of the table that contains values from JavaBean properties.
     * @throws IntrospectionException The specified type is not JavaBean.
     * @throws ReflectiveOperationException Some exception occurs in property getter.
     */
    public static <T> Table<String> create(final Class<T> rowType,
                                           final Collection<T> rows,
                                           final Set<String> columns) throws IntrospectionException, ReflectiveOperationException {
        if(rowType == null) return null;
        else if(columns == null) return null;
        else if(rows == null) return null;
        else {
            final List<PropertyDescriptor> cols = new ArrayList<>(columns.size());
            for(final PropertyDescriptor descr: Introspector.getBeanInfo(rowType).getPropertyDescriptors())
                if(columns.contains(descr.getName()))
                    cols.add(descr);
            return create(cols, rows);
        }
    }

    /**
     * Creates a new table from an array of JavaBeans.
     * @param rowType The type of the JavaBean.
     * @param rows An array of objects.
     * @param columns An set of JavaBean property names to be used as columns.
     * @param <T> Type of the row.
     * @return A new instance of the table that contains values from JavaBean properties.
     * @throws IntrospectionException The specified type is not JavaBean.
     * @throws ReflectiveOperationException Some exception occurs in property getter.
     */
    public static <T> Table<String> create(final Class<T> rowType,
                                           final T[] rows,
                                           final Set<String> columns) throws IntrospectionException, ReflectiveOperationException {
        return rows != null ? create(rowType, Arrays.asList(rows), columns) : null;
    }

    /**
     * Creates a new table from an array of JavaBeans.
     * @param rowType The type of the JavaBean.
     * @param rows An array of objects.
     * @param columns An array of JavaBean property names to be used as columns.
     * @param <T> Type of the row.
     * @return A new instance of the table that contains values from JavaBean properties.
     * @throws IntrospectionException The specified type is not JavaBean.
     * @throws ReflectiveOperationException Some exception occurs in property getter.
     */
    public static <T> Table<String> create(final Class<T> rowType,
                                           final T[] rows,
                                           final String... columns) throws IntrospectionException, ReflectiveOperationException {
        return create(rowType, rows, new HashSet<>(Arrays.asList(columns)));
    }

    private static <T> List<T> fromTable(final Supplier<T> beanFactory,
                                         final Collection<PropertyDescriptor> columns,
                                         final Table<String> table) throws IntrospectionException, ReflectiveOperationException{
        final List<T> result = new ArrayList<>(table.getRowCount());
        for (int i = 0; i < table.getRowCount(); i++) {
            final T newRow = beanFactory.get();
            for (final PropertyDescriptor col : columns)
                col.getWriteMethod().invoke(newRow, table.getCell(col.getName(), i));
            result.add(newRow);
        }
        return result;
    }

    public static <T> List<T> fromTable(final Class<T> rowType,
                                        final Supplier<T> beanFactory,
                                        final Table<String> table) throws IntrospectionException, ReflectiveOperationException {
        if (rowType == null || table == null) return Collections.emptyList();
        else {
            final Collection<PropertyDescriptor> columns = new ArrayList<>(table.getColumns().size());
            for (final PropertyDescriptor descr : Introspector.getBeanInfo(rowType).getPropertyDescriptors())
                if (table.getColumns().contains(descr.getName()))
                    columns.add(descr);
            return fromTable(beanFactory, columns, table);
        }
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
