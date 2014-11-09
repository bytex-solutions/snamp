package com.itworks.snamp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * Represents a set of factory methods for creating strongly-typed
 * in-memory tables implementing {@link com.itworks.snamp.Table} interface.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TableFactory<COLUMN> {

    /**
     * Represents default row capacity.
     */
    public static final int DEFAULT_ROW_CAPACITY = 10;

    /**
     * Represents table factory that can produce instances of
     * {@link com.itworks.snamp.InMemoryTable}&lt;{@link java.lang.String}&gt; class.
     */
    public static final TableFactory<String> STRING_TABLE_FACTORY = new TableFactory<String>() {
        @Override
        public InMemoryTable<String> create(final ImmutableMap<String, Class<?>> columns, final int rowCapacity) {
            return new InMemoryTable<String>(columns, rowCapacity) { };
        }
    };

    public static final TableFactory<Integer> INTEGER_TABLE_FACTORY = new TableFactory<Integer>() {
        @Override
        public InMemoryTable<Integer> create(final ImmutableMap<Integer, Class<?>> columns, final int rowCapacity) {
            return new InMemoryTable<Integer>(columns, rowCapacity) { };
        }
    };

    /**
     * Initializes a new table factory.
     */
    protected TableFactory() {
    }

    /**
     * Creates a new empty table with specified columns and row capacity.
     * @param columns The columns of the table. Cannot be {@literal null}.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public abstract InMemoryTable<COLUMN> create(final ImmutableMap<COLUMN, Class<?>> columns,
                                                 final int rowCapacity);

    /**
     * Creates a new instance of in-memory table.
     * @param <E> Type of the exception that can be produced by builder.
     * @param builder The column builder with constructed columns.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     * @throws E Some error occurred in builder.
     */
    public final <E extends Exception> InMemoryTable<COLUMN> create(final Consumer<ImmutableMap.Builder<COLUMN, Class<?>>, E> builder,
                                                                       final int rowCapacity) throws E {
        final ImmutableMap.Builder<COLUMN, Class<?>> b = ImmutableMap.builder();
        builder.accept(b);
        return create(b.build(), rowCapacity);
    }

    private InMemoryTable<COLUMN> create(final Iterator<COLUMN> columns,
                                         final int columnCount,
                                         final Class<?> columnType,
                                         final int rowCapacity){
        final ImmutableMap<COLUMN, Class<?>> cols;
        switch (columnCount) {
            case 0:
                cols = ImmutableMap.of();
                break;
            case 1:
                cols = ImmutableMap.<COLUMN, Class<?>>of(columns.next(),
                        columnType);
                break;
            case 2:
                cols = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType);
                break;
            case 3:
                cols = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                break;
            case 4:
                cols = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                break;
            case 5:
                cols = ImmutableMap.of(columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType,
                        columns.next(), columnType);
                break;
            default:
                final ImmutableMap.Builder<COLUMN, Class<?>> builder = ImmutableMap.builder();
                while (columns.hasNext())
                    builder.put(columns.next(), columnType);
                cols = builder.build();
                break;
        }
        return create(cols, rowCapacity);
    }

    /**
     * Creates a new table with identical column type.
     * @param columns A collection of columns.
     * @param columnType The type of all columns.
     * @param rowCapacity Expected count of rows.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final Collection<COLUMN> columns,
                                                 final Class<?> columnType,
                                                 final int rowCapacity) {
        return create(columns.iterator(), columns.size(), columnType, rowCapacity);
    }

    /**
     * Creates a new table using the specified collection of columns and rows.
     * @param columns A map for columns.
     * @param rows A map of rows.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final ImmutableMap<COLUMN, Class<?>> columns,
                                              final Collection<Map<COLUMN, Object>> rows){
        final InMemoryTable<COLUMN> result = create(columns, rows.size());
        result.addAll(rows);
        return result;
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
     * Creates a new table with the specified columns.
     * @param columns An array of table columns.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final Map.Entry<COLUMN, Class<?>>[] columns,
                                              final int rowCapacity){
        final ImmutableMap<COLUMN, Class<?>> cols;
        switch (columns.length) {
            case 0:
                cols = ImmutableMap.of();
                break;
            case 1:
                cols = createColumns(columns[0]);
                break;
            case 2:
                cols = createColumns(columns[0], columns[1]);
                break;
            case 3:
                cols = createColumns(columns[0], columns[1], columns[2]);
                break;
            case 4:
                cols = createColumns(columns[0], columns[1], columns[2], columns[3]);
                break;
            case 5:
                cols = createColumns(columns[0], columns[1], columns[2], columns[3], columns[4]);
                break;
            default:
                ImmutableMap.Builder<COLUMN, Class<?>> builder = ImmutableMap.builder();
                for (final Map.Entry<COLUMN, Class<?>> column : columns)
                    builder = builder.put(column);
                cols = builder.build();
                break;
        }
        return create(cols, rowCapacity);
    }

    /**
     * Creates a new empty table with the specified columns and default row capacity.
     * @param columns An array of columns.
     * @return A new table instance.
     * @see #DEFAULT_ROW_CAPACITY
     * @see #create(java.util.Map.Entry[], int)
     */
    @SafeVarargs
    public final InMemoryTable<COLUMN> create(final Map.Entry<COLUMN, Class<?>>... columns){
        return create(columns, DEFAULT_ROW_CAPACITY);
    }

    /**
     * Creates a new empty table with the specified columns.
     * <p>
     *     This method creates a new copy of the passed {@link java.util.Map}.
     * @param columns A map of table columns.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final Map<COLUMN, Class<?>> columns,
                                              final int rowCapacity){
        return create(ImmutableMap.copyOf(columns), rowCapacity);
    }

    /**
     * Creates a new empty table with the specified columns and default row capacity.
     * <p>
     *     This method creates a new copy of the passed {@link java.util.Map}.
     * @param columns A map of table columns.
     * @return A new table instance.
     * @see #DEFAULT_ROW_CAPACITY
     * @see #create(java.util.Map, int)
     */
    public final InMemoryTable<COLUMN> create(final Map<COLUMN, Class<?>> columns){
        return create(columns, DEFAULT_ROW_CAPACITY);
    }

    /**
     * Creates a new table with single column.
     * @param columnId The identifier of the column.
     * @param columnType The column type.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final COLUMN columnId,
                                              final Class<?> columnType,
                                              final int rowCapacity){
        return create(ImmutableMap.<COLUMN, Class<?>>of(columnId, columnType), rowCapacity);
    }

    /**
     * Creates a new table with two columns.
     * @param columnId1 The identifier of the first column.
     * @param columnType1 The type of the first column.
     * @param columnId2 The identifier of the second column.
     * @param columnType2 The type of the second column.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final COLUMN columnId1, final Class<?> columnType1,
                                              final COLUMN columnId2, final Class<?> columnType2,
                                              final int rowCapacity){
        return create(ImmutableMap.of(columnId1, columnType1,
                        columnId2, columnType2),
                rowCapacity);
    }

    /**
     * Creates a new table with three columns.
     * @param columnId1 The identifier of the first column.
     * @param columnType1 The type of the first column.
     * @param columnId2 The identifier of the second column.
     * @param columnType2 The type of the second column.
     * @param columnId3 The identifier of the third column.
     * @param columnType3 The type of the third column.
     * @param rowCapacity The initial capacity of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> create(final COLUMN columnId1, final Class<?> columnType1,
                                              final COLUMN columnId2, final Class<?> columnType2,
                                              final COLUMN columnId3, final Class<?> columnType3,
                                              final int rowCapacity){
        return create(ImmutableMap.of(columnId1, columnType1,
                        columnId2, columnType2,
                        columnId3, columnType3),
                rowCapacity);
    }

    /**
     * Creates a new table with single row and automatically infers
     * the type of each column using the value of the cell in the row.
     * @param row The row of the table.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> fromSingleRow(final Map<COLUMN, ?> row) {
        final InMemoryTable<COLUMN> result = create(new SafeConsumer<ImmutableMap.Builder<COLUMN, Class<?>>>() {
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
     * Creates a new table where each column typed with {@link java.lang.Object} class.
     * @param rows An array of rows.
     * @return A new table instance.
     */
    public final InMemoryTable<COLUMN> fromArray(final Map<COLUMN, ?>[] rows){
        if(rows.length == 0) return new InMemoryTable<>(ImmutableMap.<COLUMN, Class<?>>of(), 1);
        InMemoryTable<COLUMN> result = null;
        for(final Map<COLUMN, ?> row: rows){
            if(result == null){
                final Map<COLUMN, Class<?>> columns = new HashMap<>(10);
                for(final COLUMN key: row.keySet())
                    columns.put(key, Object.class);
                result = create(columns, rows.length);
            }
            result.addRow(row);
        }
        return result;
    }

    private static Table<String> fromBeans(final List<PropertyDescriptor> columns,
                                        final Collection<?> rows) throws ReflectiveOperationException {
        final Table<String> result = STRING_TABLE_FACTORY.create(new SafeConsumer<ImmutableMap.Builder<String, Class<?>>>() {
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
    public static <T> Table<String> fromBeans(final Class<T> rowType,
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
            return fromBeans(cols, rows);
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
    public static <T> Table<String> fromBeans(final Class<T> rowType,
                                           final T[] rows,
                                           final Set<String> columns) throws IntrospectionException, ReflectiveOperationException {
        return rows != null ? fromBeans(rowType, Arrays.asList(rows), columns) : null;
    }

    /**
     * Creates a new table from an array of JavaBeans.
     * @param rowType The type of the JavaBean.
     * @param rows An array of objects.
     * @param columns An array of JavaBean property names to be used as columns.
     * @param <T> Type of the row.
     * @return A new instance of the table that contains values from JavaBean properties.
     * @throws java.beans.IntrospectionException The specified type is not JavaBean.
     * @throws ReflectiveOperationException Some exception occurs in property getter.
     */
    public static <T> Table<String> fromBeans(final Class<T> rowType,
                                           final T[] rows,
                                           final String... columns) throws IntrospectionException, ReflectiveOperationException {
        return fromBeans(rowType, rows, new HashSet<>(Arrays.asList(columns)));
    }

    private static <T> List<T> toBeans(final Supplier<T> beanFactory,
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

    /**
     * Converts the specified table into list of JavaBeans.
     * @param rowType The JavaBean that describes the row schema.
     * @param beanFactory A factory that is used to create JavaBean for each row.
     * @param table A table to convert.
     * @param <T> JavaBean type.
     * @return A new list of initialized JavaBeans.
     * @throws IntrospectionException Unable to reflect JavaBean type.
     * @throws ReflectiveOperationException Unable to invoke setter on JavaBean instance.
     */
    public static <T> List<T> toBeans(final Class<T> rowType,
                                          final Supplier<T> beanFactory,
                                          final Table<String> table) throws IntrospectionException, ReflectiveOperationException {
        if (rowType == null || table == null) return Collections.emptyList();
        else {
            final Collection<PropertyDescriptor> columns = new ArrayList<>(table.getColumns().size());
            for (final PropertyDescriptor descr : Introspector.getBeanInfo(rowType).getPropertyDescriptors())
                if (table.getColumns().contains(descr.getName()))
                    columns.add(descr);
            return toBeans(beanFactory, columns, table);
        }
    }
}
