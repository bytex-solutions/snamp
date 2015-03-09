package com.itworks.snamp.jmx;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.ArrayUtils;

import javax.management.openmbean.*;
import java.util.*;

/**
 * Represents builder of {@link javax.management.openmbean.TabularType} instances.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see javax.management.openmbean.TabularType
 */
public final class TabularTypeBuilder extends AbstractAggregator implements OpenTypeBuilder<TabularType>, Iterable<String> {
    private String typeName;
    private String typeDescription;
    private final CompositeTypeBuilder rowBuilder;
    private final Set<String> indexes;
    private Object service;

    private static String createRowTypeName(final String tabularTypeName){
        return tabularTypeName + "Row";
    }

    /**
     * Initializes a new tabular type builder.
     * @param typeName The name of the tabular type. Cannot be {@literal null}.
     * @param typeDescription The description of the tabular type. Cannot be {@literal null}.
     * @param rowTypeName The name of the row type. Cannot be {@literal null}.
     * @param rowTypeDescription The description of the row type. Cannot be {@literal null}.
     */
    public TabularTypeBuilder(final String typeName,
                              final String typeDescription,
                              final String rowTypeName,
                              final String rowTypeDescription){
        this.typeName = Objects.requireNonNull(typeName, "typeName is null.");
        this.typeDescription = Objects.requireNonNull(typeDescription, "typeDescription is null.");
        this.rowBuilder = new CompositeTypeBuilder(rowTypeName, rowTypeDescription);
        this.indexes = new HashSet<>(CompositeTypeBuilder.DEFAULT_CAPACITY);
    }

    public TabularTypeBuilder(final String typeName,
                              final String typeDescription) {
        this(typeName, typeDescription, createRowTypeName(typeName), typeDescription);
    }

    /**
     * Initializes a new empty tabular type builder.
     */
    public TabularTypeBuilder(){
        this("", "", "", "");
    }

    final void setService(final Object obj){
        this.service = obj;
    }

    final String getTypeName(){
        return typeName;
    }

    /**
     * Sets name of the tabular type.
     * @param value The name of the tabular type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final TabularTypeBuilder setTypeName(final String value){
        this.typeName = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    public final TabularTypeBuilder setTypeName(final String value, final boolean setRowTypeName) {
        return setRowTypeName ?
                setTypeName(value).setRowTypeName(createRowTypeName(value)) :
                setTypeName(value);
    }

    public final TabularTypeBuilder setDescription(final String value, final boolean setRowDescription) {
        return setRowDescription ?
                setDescription(value).setRowDescription(value) :
                setDescription(value);
    }

    final String getDescription(){
        return typeDescription;
    }

    /**
     * Sets description of the tabular type.
     * @param value The description of the tabular type. Cannot be {@literal null}.
     * @return This builder.
     */
    public final TabularTypeBuilder setDescription(final String value){
        this.typeDescription = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    final String getRowTypeName(){
        return rowBuilder.getTypeName();
    }

    /**
     * Sets name of the row type.
     * @param value The name of the row type. Cannot be {@literal null} or empty.
     * @return This builder
     */
    public final TabularTypeBuilder setRowTypeName(final String value){
        rowBuilder.setTypeName(value);
        return this;
    }

    final String getRowDescription(){
        return rowBuilder.getDescription();
    }

    public final TabularTypeBuilder addColumn(final String columnName,
                          final String columnDescription,
                          final OpenType<?> columnType,
                          final boolean indexed){
        rowBuilder.addItem(columnName, columnDescription, columnType);
        if(indexed)
            indexes.add(columnName);
        return this;
    }

    /**
     * Sets description of the row type.
     * @param value The description of the row type. Cannot be {@literal null}.
     * @return This builder.
     */
    public final TabularTypeBuilder setRowDescription(final String value){
        rowBuilder.setDescription(value);
        return this;
    }

    final CompositeType buildRowType() throws OpenDataException {
        return rowBuilder.build();
    }

    final int size(){
        return rowBuilder.size();
    }

    /**
     * Constructs a new table row.
     * @param cells The cells of the row.
     * @return A new row.
     * @throws OpenDataException Unable to construct the row.
     */
    public final CompositeData buildRow(final Map<String, ?> cells) throws OpenDataException {
        return rowBuilder.build(cells);
    }

    /**
     * Constructs a new instance of {@link javax.management.openmbean.TabularType}.
     * @return A new instance of {@link javax.management.openmbean.TabularType}.
     * @throws OpenDataException Unable to construct type.
     */
    public final TabularType build() throws OpenDataException{
        return new TabularType(typeName,
                typeDescription,
                buildRowType(),
                ArrayUtils.toArray(indexes, String.class));
    }

    /**
     * Constructs a new instance of {@link javax.management.openmbean.TabularType}.
     * @return A new instance of {@link javax.management.openmbean.TabularType}.
     * @throws java.lang.IllegalStateException Unable to construct type.
     */
    @Override
    public final TabularType get() throws IllegalStateException{
        try {
            return build();
        } catch (final OpenDataException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns an iterator over a set of columns.
     *
     * @return an Iterator.
     */
    @Override
    public final Iterator<String> iterator() {
        return rowBuilder.iterator();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return objectType.isInstance(service) ? objectType.cast(service) : null;
    }
}
