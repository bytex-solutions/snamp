package com.bytex.snamp.jmx;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.util.*;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents builder of {@link javax.management.openmbean.TabularType} instances.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see javax.management.openmbean.TabularType
 */
public final class TabularTypeBuilder implements OpenTypeBuilder<TabularType>, Iterable<String>, Serializable {
    private static final long serialVersionUID = -3909990870658711437L;
    private String typeName;
    private String typeDescription;
    private final CompositeTypeBuilder rowBuilder;
    private final Set<String> indexes;

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

    String getTypeName(){
        return typeName;
    }

    /**
     * Sets name of the tabular type.
     * @param value The name of the tabular type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public TabularTypeBuilder setTypeName(final String value){
        this.typeName = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    public TabularTypeBuilder setTypeName(final String value, final boolean setRowTypeName) {
        return setRowTypeName ?
                setTypeName(value).setRowTypeName(createRowTypeName(value)) :
                setTypeName(value);
    }

    public TabularTypeBuilder setDescription(final String value, final boolean setRowDescription) {
        return setRowDescription ?
                setDescription(value).setRowDescription(value) :
                setDescription(value);
    }

    String getDescription(){
        return typeDescription;
    }

    /**
     * Sets description of the tabular type.
     * @param value The description of the tabular type. Cannot be {@literal null}.
     * @return This builder.
     */
    public TabularTypeBuilder setDescription(final String value){
        this.typeDescription = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    String getRowTypeName(){
        return rowBuilder.getTypeName();
    }

    /**
     * Sets name of the row type.
     * @param value The name of the row type. Cannot be {@literal null} or empty.
     * @return This builder
     */
    public TabularTypeBuilder setRowTypeName(final String value){
        rowBuilder.setTypeName(value);
        return this;
    }

    String getRowDescription(){
        return rowBuilder.getDescription();
    }

    public TabularTypeBuilder addColumn(final String columnName,
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
    public TabularTypeBuilder setRowDescription(final String value){
        rowBuilder.setDescription(value);
        return this;
    }

    private CompositeType buildRowType() throws OpenDataException {
        return rowBuilder.build();
    }

    int size(){
        return rowBuilder.size();
    }

    /**
     * Constructs a new table row.
     * @param cells The cells of the row.
     * @return A new row.
     * @throws OpenDataException Unable to construct the row.
     */
    public CompositeData buildRow(final Map<String, ?> cells) throws OpenDataException {
        return rowBuilder.build(cells);
    }

    /**
     * Constructs a new instance of {@link javax.management.openmbean.TabularType}.
     * @return A new instance of {@link javax.management.openmbean.TabularType}.
     * @throws OpenDataException Unable to construct type.
     */
    public TabularType build() throws OpenDataException{
        return new TabularType(typeName,
                typeDescription,
                buildRowType(),
                indexes.stream().toArray(String[]::new));
    }

    /**
     * Returns an iterator over a set of columns.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<String> iterator() {
        return rowBuilder.iterator();
    }
}
