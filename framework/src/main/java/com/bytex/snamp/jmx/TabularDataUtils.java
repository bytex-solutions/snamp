package com.bytex.snamp.jmx;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides helper methods that allows to create and
 * manipulate of {@link javax.management.openmbean.TabularData} instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class TabularDataUtils {
    private static final class RowList<R extends CompositeDataBean> extends ArrayList<R>{
        private static final long serialVersionUID = 2584942280684733271L;
        private final TabularType type;

        private RowList(final int size, final TabularType type){
            super(size + 5);
            this.type = Objects.requireNonNull(type);
        }
    }

    private TabularDataUtils(){

    }

    public static <R extends CompositeDataBean> RowList<R> convert(final Class<R> rowType,
                                                                final TabularData table) throws ReflectiveOperationException {
        final RowList<R> rows = new RowList<>(table.size(), table.getTabularType());
        for(final Object row: table.values())
            if(row instanceof CompositeData)
                rows.add(CompositeDataUtils.convert(rowType, (CompositeData)row));
        return rows;
    }

    public static <R extends CompositeDataBean, T extends TabularDataBean<R>> T convert(final Class<T> tableType,
                                                                                        final Class<R> rowType,
                                                                                        final TabularData table) throws ReflectiveOperationException{
        final T result = tableType.newInstance();
        for(final Object row: table.values())
            if(row instanceof CompositeData)
                result.add(CompositeDataUtils.convert(rowType, (CompositeData) row));
        return result;
    }

    private static TabularData convert(final RowList<?> rows) throws OpenDataException, ReflectiveOperationException {
        final TabularDataSupport result = new TabularDataSupport(rows.type);
        for(final CompositeDataBean row: rows)
            result.put(CompositeDataUtils.convert(row, rows.type.getRowType()));
        return result;
    }

    public static TabularData convert(final List<? extends CompositeDataBean> rows) throws IllegalArgumentException, ReflectiveOperationException, OpenDataException {
        if(rows instanceof RowList<?>)
            return convert((RowList<?>)rows);
        else throw new IllegalArgumentException("rows are invalid.");
    }

    public static TabularData convert(final TabularDataBean<?> table,
                                      final TabularType type) throws OpenDataException, ReflectiveOperationException {
        final TabularDataSupport result = new TabularDataSupport(type);
        for(final CompositeDataBean row: table)
            result.put(CompositeDataUtils.convert(row, type.getRowType()));
        return result;
    }

    public static <E extends Throwable> void forEachRow(final TabularData table, final Acceptor<CompositeData, E> rowReader) throws E{
        for(final Object row: table.values())
            if(row instanceof CompositeData)
                rowReader.accept((CompositeData)row);
    }

    public static List<CompositeData> getRows(final TabularData table){
        final ArrayList<CompositeData> rows = Lists.newArrayListWithExpectedSize(table.size());
        forEachRow(table, rows::add);
        return rows;
    }

    private static void checkKeyValuePairType(final TabularType type) throws OpenDataException {
        if (type.getRowType().keySet().size() != 2 || type.getIndexNames().size() != 1)
            throw new OpenDataException("Incorrect type for key/value pairs " + type);
    }

    private static void getKeyValueColumn(final TabularType type, final Consumer<String> keyColumn, final Consumer<String> valueColumn){
        for (final String keyName : type.getRowType().keySet())
            if (type.getIndexNames().contains(keyName))
                keyColumn.accept(keyName);
            else
                valueColumn.accept(keyName);
    }

    public static TabularData makeKeyValuePairs(final TabularType type, final Map<?, ?> pairs) throws OpenDataException {
        checkKeyValuePairType(type);
        //check entry type: one column must be indexed and another not
        final Box<String> indexName = BoxFactory.create("");
        final Box<String> valueName = BoxFactory.create("");
        getKeyValueColumn(type, indexName, valueName);
        assert !isNullOrEmpty(indexName.get()) : indexName;
        assert !isNullOrEmpty(valueName.get()) : valueName;
        final TabularDataSupport result = new TabularDataSupport(type);
        for (final Map.Entry<?, ?> entry : pairs.entrySet())
            if (entry.getValue() != null)
                result.put(new CompositeDataSupport(type.getRowType(), ImmutableMap.of(indexName.get(), entry.getKey(), valueName.get(), entry.getValue())));
        return result;
    }

    public static Map<?, ?> makeKeyValuePairs(final TabularData table) throws OpenDataException {
        checkKeyValuePairType(table.getTabularType());
        final Box<String> keyColumn = BoxFactory.create("");
        final Box<String> valueColumn = BoxFactory.create("");
        getKeyValueColumn(table.getTabularType(), keyColumn, valueColumn);
        assert !isNullOrEmpty(keyColumn.get()) : keyColumn;
        assert !isNullOrEmpty(valueColumn.get()) : valueColumn;
        final Map<Object, Object> result = Maps.newHashMapWithExpectedSize(table.size());
        forEachRow(table, row -> {
            final String keyColumnName = keyColumn.get();
            final String valueColumnName = valueColumn.get();
            result.put(row.get(keyColumnName), row.get(valueColumnName));
        });
        return result;
    }
}
