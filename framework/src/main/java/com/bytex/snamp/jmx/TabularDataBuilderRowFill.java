package com.bytex.snamp.jmx;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.util.Map;

/**
 * Represents row constructor for {@link javax.management.openmbean.TabularData}.
 * <p>
 *     This class differs from {@link com.bytex.snamp.jmx.TabularDataBuilder}
 *     because it can be used in scenario with prepared {@link javax.management.openmbean.TabularType}.
 *     {@link com.bytex.snamp.jmx.TabularDataBuilder} cannot be used with prepared
 *     {@link javax.management.openmbean.TabularType}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class TabularDataBuilderRowFill implements Supplier<TabularData> {
    /**
     * Represents row builder.
     * This class cannot be inherited or instantiated directly from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    public final class RowBuilder{
        private final ImmutableMap.Builder<String, Object> cells;

        private RowBuilder(){
            cells = ImmutableMap.builder();
        }

        public RowBuilder cell(final String name, final Number value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final boolean value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final String value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final String[] value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final char value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final ObjectName value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final CompositeData value){
            cells.put(name, value);
            return this;
        }

        public RowBuilder cell(final String name, final TabularData value){
            cells.put(name, value);
            return this;
        }

        /**
         * Flushes the row to the underlying table.
         * @return The underlying table row builder.
         * @throws OpenDataException
         */
        public TabularDataBuilderRowFill flush() throws OpenDataException {
            TabularDataBuilderRowFill.this.addRow(cells.build());
            return TabularDataBuilderRowFill.this;
        }
    }

    private final TabularDataSupport table;


    /**
     * Initializes a new row builder.
     * @param type The type of the table. Cannot be {@literal null}.
     */
    public TabularDataBuilderRowFill(final TabularType type){
        table = new TabularDataSupport(type);
    }

    /**
     * Adds a new row to the table.
     * @param values Row values. Cannot be {@literal null}.
     * @throws OpenDataException Row values doesn't match to the row type.
     */
    public final void addRow(final Map<String, ?> values) throws OpenDataException {
        final CompositeType rowType = table.getTabularType().getRowType();
        table.put(new CompositeDataSupport(rowType, values));
    }

    /**
     * Constructs a new row in the table.
     * @return A builder for a new row in the table.
     */
    public final RowBuilder newRow(){
        return new RowBuilder();
    }

    /**
     * Retrieves underlying {@link javax.management.openmbean.TabularData} with
     * constructed rows.
     *
     * @return {@link javax.management.openmbean.TabularData} with rows.
     */
    @Override
    public TabularData get() {
        return table;
    }

    public TabularData build(){
        return get();
    }
}
