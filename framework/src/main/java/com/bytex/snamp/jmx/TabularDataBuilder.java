package com.bytex.snamp.jmx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Represents {@link javax.management.openmbean.TabularData} instance builder.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class TabularDataBuilder extends LinkedList<CompositeData> implements Supplier<TabularData> {
    private static final long serialVersionUID = 6161683440252406652L;
    private final TabularTypeBuilder columns;

    public TabularDataBuilder(){
        columns = new TabularTypeBuilder();
        columns.setService(this);
    }

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

    /**
     * Returns builder for columns.
     * @return The builder for columns.
     */
    public final TabularTypeBuilder columns(){
        return columns;
    }

    private TabularDataBuilder add(final Iterator<?> cells) throws OpenDataException {
        final Iterator<String> columns = columns().iterator();
        final Map<String, Object> row = Maps.newHashMapWithExpectedSize(columns().size());
        while (cells.hasNext() && columns.hasNext())
            row.put(columns.next(), cells.next());
        add(columns().buildRow(row));
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

    /**
     * Constructs a new {@link javax.management.openmbean.TabularData} instance.
     * @return A new {@link javax.management.openmbean.TabularData} instance.
     * @throws IllegalStateException Unable to construct table.
     */
    @Override
    public final TabularData get() throws IllegalStateException{
        try {
            return build();
        } catch (final OpenDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
