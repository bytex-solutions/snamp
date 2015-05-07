package com.itworks.snamp.jmx;

import com.google.common.collect.Lists;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides helper methods that allows to create and
 * manipulate of {@link javax.management.openmbean.TabularData} instances.
 * @author Roman Sakno
 * @version 1.0
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

    public static <E extends Throwable> void forEachRow(final TabularData table, final Consumer<CompositeData, E> rowReader) throws E{
        for(final Object row: table.values())
            if(row instanceof CompositeData)
                rowReader.accept((CompositeData)row);
    }

    public static List<CompositeData> getRows(final TabularData table){
        final ArrayList<CompositeData> rows = Lists.newArrayListWithExpectedSize(table.size());
        forEachRow(table, new SafeConsumer<CompositeData>() {
            @Override
            public void accept(final CompositeData row) {
                rows.add(row);
            }
        });
        return rows;
    }
}
