package com.itworks.snamp;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.Flat3Map;
import org.apache.commons.collections4.map.HashedMap;

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
public class SimpleTable<COLUMN> extends ArrayList<Map<COLUMN, Object>> implements Table<COLUMN> {
    private static final class ColumnBuilder<COLUMN> implements Put<COLUMN, Class<?>>{
        private IterableMap<COLUMN, Class<?>> columns;

        private ColumnBuilder(final int columnsCount){
            columns = new HashedMap<>(columnsCount);
        }

        private Map<COLUMN, Class<?>> build(final Closure<Put<COLUMN, Class<?>>> filler) {
            filler.execute(this);
            final Map<COLUMN, Class<?>> result = columns;
            columns = null;
            return result;
        }

        private void checkInternalBuffer(){
            if(columns == null)
                throw new IllegalStateException("The column builder is closed.");
        }

        @Override
        public void clear() {
            checkInternalBuffer();
            columns.clear();
        }

        @Override
        public Object put(final COLUMN key, final Class<?> value) {
            checkInternalBuffer();
            return columns.put(key, value);
        }

        @Override
        public void putAll(final Map<? extends COLUMN, ? extends Class<?>> t) {
            checkInternalBuffer();
            columns.putAll(t);
        }
    }

    private final Map<COLUMN, Class<?>> _columns;


    /**
     * Initializes a new table using the column builder.
     * <p>
     *     Note that the builder will be cleaned inside of the constructor.
     * @param builder The column builder with constructed columns.
     * @param columnsCount Expected count of columns.
     * @param rowCapacity The initial capacity of the table.
     */
    public SimpleTable(final Closure<Put<COLUMN, Class<?>>> builder,
                       final int columnsCount,
                       final int rowCapacity) {
        super(rowCapacity);
        final ColumnBuilder<COLUMN> factory = new ColumnBuilder<>(columnsCount);
        _columns = factory.build(builder);
    }

    /**
     * Initializes a new table using the specified collection of columns and rows.
     * <p>
     *     This constructor saves passed map with columns as-is without copying the
     *     key/value pairs.
     * @param columns A map for columns.
     * @param rows A map of rows.
     */
    public SimpleTable(final Map<COLUMN, Class<?>> columns,
                       final Collection<Map<COLUMN, Object>> rows) {
        this(new Factory<Map<COLUMN, Class<?>>>() {
            @Override
            public Map<COLUMN, Class<?>> create() {
                return columns;
            }
        }, rows.size());
        addAll(rows);
    }

    /**
     * Initializes a new simple table with the specified set of columns.
     * @param cols An array of unique columns.
     */
    @SafeVarargs
    public SimpleTable(final Map.Entry<COLUMN, Class<?>>... cols) {
        this(new Factory<Map<COLUMN, Class<?>>>() {
            @Override
            public Map<COLUMN, Class<?>> create() {
                return new HashMap<>(cols.length);
            }
        }, 5);
        for (final Map.Entry<COLUMN, Class<?>> c : cols)
            _columns.put(c.getKey(), c.getValue());
    }

    /**
     * Initializes a new simple table with the specified collection of columns.
     * @param columns A collection of unique columns.
     */
    public SimpleTable(final Map<COLUMN, Class<?>> columns){
        this(columns, 5);
    }

    /**
     * Initializes a new table with the specified columns and initial row capacity.
     * @param columns A collection of unique columns.
     * @param rowCapacity Initial row capacity.
     */
    public SimpleTable(final Map<COLUMN, Class<?>> columns, final int rowCapacity) {
        this(new Factory<Map<COLUMN, Class<?>>>() {
            @Override
            public Map<COLUMN, Class<?>> create() {
                return new HashMap<>(columns);
            }
        }, rowCapacity);
    }

    /**
     * Initializes a new table with the specified factory for the map with columns.
     * @param columnsFactory The factory for the columns.
     * @param rowCapacity Initial row capacity.
     */
    protected SimpleTable(final Factory<Map<COLUMN, Class<?>>> columnsFactory, final int rowCapacity){
        super(rowCapacity);
        _columns = columnsFactory.create();
    }

    /**
     * Initializes a new table with single column.
     * @param columnId The identifier of the column.
     * @param columnType The column type.
     * @param rowCapacity The initial capacity of the table.
     */
    public SimpleTable(final COLUMN columnId, final Class<?> columnType, final int rowCapacity) {
        super(rowCapacity);
        _columns = Collections.<COLUMN, Class<?>>singletonMap(columnId, columnType);
    }

    /**
     * Initializes a new table with two columns.
     * @param columnId1 The identifier of the first column.
     * @param columnType1 The type of the first column.
     * @param columnId2 The identifier of the second column.
     * @param columnType2 The type of the second column.
     * @param rowCapacity The initial capacity of the table.
     */
    public SimpleTable(final COLUMN columnId1, final Class<?> columnType1,
                       final COLUMN columnId2, final Class<?> columnType2,
                       final int rowCapacity){
        this(new Factory<Map<COLUMN, Class<?>>>() {
            @Override
            public Flat3Map<COLUMN, Class<?>> create() {
                return new Flat3Map<>();
            }
        }, rowCapacity);
        _columns.put(columnId1, columnType1);
        _columns.put(columnId2, columnType2);
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
    public SimpleTable(final COLUMN columnId1, final Class<?> columnType1,
                       final COLUMN columnId2, final Class<?> columnType2,
                       final COLUMN columnId3, final Class<?> columnType3,
                       final int rowCapacity){
        this(new Factory<Map<COLUMN, Class<?>>>() {
            @Override
            public Flat3Map<COLUMN, Class<?>> create() {
                return new Flat3Map<COLUMN, Class<?>>();
            }
        },
        rowCapacity);
        _columns.put(columnId1, columnType1);
        _columns.put(columnId2, columnType2);
        _columns.put(columnId3, columnType3);
    }

    /**
     * Creates a simple table from an array of rows.
     * @param rows An array of rows.
     * @param <COLUMN> Type of the column descriptor.
     * @return A new instance of the in-memory table.
     */
    public static <COLUMN> SimpleTable<COLUMN> fromArray(final Map<COLUMN, Object>[] rows){
        if(rows.length == 0) return new SimpleTable<>();
        SimpleTable<COLUMN> result = null;
        for(final Map<COLUMN, Object> row: rows){
            if(result == null){
                final Map<COLUMN, Class<?>> columns = new HashMap<>(10);
                for(final COLUMN key: row.keySet())
                    columns.put(key, Object.class);
                result = new SimpleTable<>(columns, rows.length);
            }
            result.addRow(row);
        }
        return result;
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
        if(row != null) row.put(column, columnType.cast(value));
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
        add(new HashMap<>(values));
    }

    /**
     * Gets row by its index.
     *
     * @param index Zero-based index of the row.
     * @return The map that represents the row.
     * @throws IndexOutOfBoundsException The specified row doesn't exist.
     */
    @Override
    public Map<COLUMN, Object> getRow(final int index) {
        return get(index);
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
                                            final Collection<?> rows) throws ReflectiveOperationException{
        final Table<String> result = new SimpleTable<>(new Factory<Map<String, Class<?>>>() {
            @Override
            public Map<String, Class<?>> create() {
                final Map<String, Class<?>> cols = new HashMap<>(columns.size());
                for(final PropertyDescriptor descr: columns)
                    cols.put(descr.getName(), descr.getPropertyType());
                return cols;
            }
        }, rows.size());
        for(final Object source: rows){
            final Map<String, Object> destination = new HashMap<>(columns.size());
            for(final PropertyDescriptor descr: columns)
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

    private static <T> List<T> fromTable(final Factory<T> beanFactory,
                                               final Collection<PropertyDescriptor> columns,
                                               final Table<String> table) throws IntrospectionException, ReflectiveOperationException{
        final List<T> result = new ArrayList<>(table.getRowCount());
        for (int i = 0; i < table.getRowCount(); i++) {
            final T newRow = beanFactory.create();
            for (final PropertyDescriptor col : columns)
                col.getWriteMethod().invoke(newRow, table.getCell(col.getName(), i));
            result.add(newRow);
        }
        return result;
    }

    public static <T> List<T> fromTable(final Class<T> rowType,
                                              final Factory<T> beanFactory,
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

    public static <T> List<T> fromTable(final Class<T> rowType,
                                              final Table<String> table) throws IntrospectionException, ReflectiveOperationException {
        try {
            return fromTable(rowType, new Factory<T>() {
                @Override
                public T create() {
                    try {
                        return rowType.newInstance();
                    } catch (final ReflectiveOperationException e) {
                        throw new FunctorException(e);
                    }
                }
            }, table);
        } catch (final FunctorException e) {
            throw new ReflectiveOperationException(e);
        }
    }
}
