package com.bytex.snamp.jmx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents {@link javax.management.openmbean.TabularData} instance builder.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class TabularDataBuilder extends LinkedList<CompositeData> {
    private static final long serialVersionUID = 6161683440252406652L;
    private final TabularTypeBuilder columns = new TabularTypeBuilder();

    public final String getTypeName(){
        return columns.getTypeName();
    }

    public TabularDataBuilder setTypeName(final String value){
        columns.setTypeName(value);
        return this;
    }

    public TabularDataBuilder setTypeName(final String value, final boolean setRowTypeName){
        columns.setTypeName(value, setRowTypeName);
        return this;
    }

    public TabularDataBuilder setTypeDescription(final String value){
        columns.setDescription(value);
        return this;
    }

    public TabularDataBuilder setTypeDescription(final String value, final boolean setRowDescription){
        columns.setDescription(value, setRowDescription);
        return this;
    }

    public String getTypeDescription(){
        return columns.getDescription();
    }

    public final String getRowTypeName(){
        return columns.getRowTypeName();
    }

    public TabularDataBuilder setRowTypeName(final String value){
        columns.setRowTypeName(value);
        return this;
    }

    public final String getRowTypeDescription(){
        return columns.getRowDescription();
    }

    public TabularDataBuilder setRowTypeDescription(final String value){
        columns.setRowDescription(value);
        return this;
    }

    public TabularDataBuilder declareColumns(final Consumer<? super TabularTypeBuilder> columnBuilder){
        columnBuilder.accept(columns);
        return this;
    }

    private TabularDataBuilder add(final Iterator<?> cells) throws OpenDataException {
        final Iterator<String> columns = this.columns.iterator();
        final Map<String, Object> row = Maps.newHashMapWithExpectedSize(this.columns.size());
        while (cells.hasNext() && columns.hasNext())
            row.put(columns.next(), cells.next());
        add(this.columns.buildRow(row));
        return this;
    }

    public TabularDataBuilder add(final Collection<?> cells) throws OpenDataException {
        return add(cells.iterator());
    }

    public final TabularDataBuilder add(final Object... cells) throws OpenDataException{
        return add(ImmutableList.copyOf(cells));
    }

    /**
     * Constructs a new {@link javax.management.openmbean.TabularData} instance.
     * @return A new {@link javax.management.openmbean.TabularData} instance.
     * @throws javax.management.openmbean.OpenDataException Unable to construct table.
     */
    public final TabularData build() throws OpenDataException{
        final TabularDataSupport result = new TabularDataSupport(this.columns.build());
        forEach(result::put);
        return result;
    }
}
