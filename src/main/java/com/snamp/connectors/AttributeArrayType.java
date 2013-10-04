package com.snamp.connectors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents single-dimensional array type as table.
 * @author roman
 */
public abstract class AttributeArrayType implements AttributeTabularType {
    /**
     * Represents name of the first column.
     */
    public static final String indexColumnName = "Index";

    /**
     * Represents name of the second column.
     */
    public static final String valueColumnName = "Value";

    /**
     * Represents a set of array columns (index and value).
     */
    public static final Set<String> columns = Collections.unmodifiableSet(new HashSet<String>(){{
        add(indexColumnName);
        add(valueColumnName);
    }});

    /**
     * Represents array element type.
     */
    protected final AttributeTypeInfo elementType;

    /**
     * Initializes a new instance of the array type descriptor.
     * @param elementType The type of the array elements.
     * @throws IllegalArgumentException elementType is {@literal null}.
     */
    protected AttributeArrayType(final AttributeTypeInfo elementType){
        if(elementType == null) throw new IllegalArgumentException("elementType is null.");
        this.elementType = elementType;
    }

    /**
     * Gets a set of dictionary keys (items).
     *
     * @return A set of dictionary keys (items).
     */
    @Override
    public final Set<String> getColumns() {
        return columns;
    }

    /**
     * Returns the type of the column.
     * @param columnName
     * @return
     */
    @Override
    public final AttributeTypeInfo getColumnType(final String columnName) {
        switch (columnName){
            case indexColumnName: return AttributePrimitiveType.INT32;
            case valueColumnName: return elementType;
            default: return null;
        }
    }

    /**
     * Determines whether the attribute value can be converted into the specified type.
     *
     * @param target The result of the conversion.
     * @param <T>    The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    @Override
    public <T> boolean canConvertTo(final Class<T> target) {
        return Object.class == target || String.class == target || (target.isArray() && elementType.canConvertFrom(target.getComponentType()));
    }

    protected abstract <T> T[] convertToArray(final Object value, final Class<T> elementType);

    /**
     * Converts the attribute value to the specified type.
     *
     * @param value  The attribute value to convert.
     * @param target The type of the conversion result.
     * @param <T>    Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    @Override
    public final <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
        if(target.isInstance(value)) return (T)value;
        else if(String.class == target) return (T)Objects.toString(value, "");
        else if(target.isArray() && elementType.canConvertFrom(target.getComponentType()) && value != null && value.getClass().isArray())
            return (T)convertToArray(value, target.getComponentType());
        else throw new IllegalArgumentException(String.format("Class %s is not supported.", target));
    }

    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     *
     * @param source The type of the value that can be converted to the attribute value.
     * @param <T>    The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    @Override
    public <T> boolean canConvertFrom(final Class<T> source) {
        return source.isArray() && elementType.canConvertFrom(source.getComponentType());
    }
}
