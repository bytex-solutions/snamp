package com.bytex.snamp.jmx;

import com.bytex.snamp.Acceptor;
import com.google.common.collect.Lists;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Provides helper methods that allows to create and
 * manipulate of {@link javax.management.openmbean.TabularData} instances.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class TabularDataUtils {
    public static final class RowList<R extends CompositeDataBean> extends ArrayList<R>{
        private static final long serialVersionUID = 2584942280684733271L;
        private final TabularType type;

        private RowList(final int size, final TabularType type){
            super(size + 5);
            this.type = Objects.requireNonNull(type);
        }
    }

    private TabularDataUtils(){
        throw new InstantiationError();
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

    public static TabularData convert(final RowList<?> rows) throws OpenDataException, ReflectiveOperationException {
        final TabularDataSupport result = new TabularDataSupport(rows.type);
        for(final CompositeDataBean row: rows)
            result.put(CompositeDataUtils.convert(row, rows.type.getRowType()));
        return result;
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
}
