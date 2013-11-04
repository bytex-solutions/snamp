package com.snamp.connectors;

import com.snamp.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents single-dimensional array type as table.
 * @author roman
 */
public class AttributeArrayType implements AttributeTabularType {
    /**
     * Represents name of the first column.
     */
    public static final String INDEX_COLUMN_NAME = "Index";

    /**
     * Represents name of the second column.
     */
    public static final String VALUE_COLUMN_NAME = "Value";

    /**
     * Represents a set of array columns (index and value).
     */
    public static final Set<String> columns = Collections.unmodifiableSet(new HashSet<String>(){{
        add(INDEX_COLUMN_NAME);
        add(VALUE_COLUMN_NAME);
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
    public AttributeArrayType(final AttributeTypeInfo elementType){
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
     * Returns the type of the array index column.
     * @return
     */
    protected AttributeJavaTypeInfo<? extends Number> getIndexColumnType(){
        final AttributePrimitiveTypeBuilder builder = new AttributePrimitiveTypeBuilder();
        return builder.createInt32Type();
    }

    /**
     * Returns the type of the column.
     * @param columnName
     * @return
     */
    @Override
    public final AttributeTypeInfo getColumnType(final String columnName) {
        switch (columnName){
            case INDEX_COLUMN_NAME: getIndexColumnType();
            case VALUE_COLUMN_NAME: return elementType;
            default: return null;
        }
    }

    /**
     * Returns the number of rows if this information is available.
     *
     * @return The count of rows.
     * @throws UnsupportedOperationException Row count is not supported.
     */
    @Override
    public long getRowCount() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether the specified object is an array.
     * @param obj
     * @return
     */
    public static boolean isArray(final Object obj) {
        return obj != null && obj.getClass().isArray();
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
        return Object.class == target ||
                String.class == target ||
                Table.class == target ||
                (target.isArray() && elementType.canConvertFrom(target.getComponentType()));
    }

    /**
     * Converts the attribute value to thw array.
     * @param value
     * @param destinationElementType
     * @param <T>
     * @return
     */
    protected <T> T[] convertToArray(final Object value, final Class<T> destinationElementType) throws IllegalArgumentException{
        if(isArray(value)){
            final Object result = Array.newInstance(destinationElementType, Array.getLength(value));
            for(int i = 0; i < Array.getLength(value); i++)
                Array.set(result, i, elementType.convertTo(Array.get(value, i), destinationElementType));
            return (T[])result;
        } else if(value instanceof List)
            return convertToArray(((List)value).toArray(), destinationElementType);
        else throw new IllegalArgumentException(String.format("Could not convert %s to array.", value));
    }

    protected Table<String> convertToTable(final Object value){
        final Table<String> result = new SimpleTable<String>(new HashMap<String, Class<?>>(2){{
            put(INDEX_COLUMN_NAME, Integer.class);
            put(VALUE_COLUMN_NAME, Object.class);
        }});
        if(isArray(value))
            for(int i = 0; i < Array.getLength(value); i++){
                final Map<String, Object> row = new HashMap<>(2);
                row.put(INDEX_COLUMN_NAME, i);
                row.put(VALUE_COLUMN_NAME, Array.get(value, i));
                result.addRow(row);
            }
        else if(value instanceof List){
            final List<?> lst = (List<?>)value;
            for(int i = 0; i < lst.size(); i++){
                final Map<String, Object> row = new HashMap<>(2);
                row.put(INDEX_COLUMN_NAME, i);
                row.put(VALUE_COLUMN_NAME, lst.get(i));
                result.addRow(row);
            }
        }
        else throw new IllegalArgumentException(String.format("Could not convert %s to table.", value));
        return result;
    }

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
    public <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
        if(target.isInstance(value)) return (T)value;
        else if(Table.class == target) return (T)convertToTable(value);
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
        return Table.class == source || (source.isArray() && elementType.canConvertFrom(source.getComponentType()));
    }
}
