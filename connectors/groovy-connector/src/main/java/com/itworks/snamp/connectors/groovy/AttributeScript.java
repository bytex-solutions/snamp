package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.CompositeDataUtils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import com.itworks.snamp.jmx.TabularTypeBuilder;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an abstract class for attribute handling script
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AttributeScript extends ResourceFeatureScript implements AttributeAccessor {
    private static final String GET_VALUE_METHOD = "getValue";
    private static final String SET_VALUE_METHOD = "setValue";
    private static final String ITEM_DESCR = "description";
    private static final String ITEM_TYPE = "type";
    private static final String ITEM_INDEXED = "indexed";
    @SpecialUse
    protected static final SimpleType<Byte> INT8 = SimpleType.BYTE;
    @SpecialUse
    protected static final SimpleType<Short> INT16 = SimpleType.SHORT;
    @SpecialUse
    protected static final SimpleType<Integer> INT32 = SimpleType.INTEGER;
    @SpecialUse
    protected static final SimpleType<Long> INT64 = SimpleType.LONG;
    @SpecialUse
    protected static final SimpleType<Float> FLOAT32 = SimpleType.FLOAT;
    @SpecialUse
    protected static final SimpleType<Double> FLOAT64 = SimpleType.DOUBLE;
    @SpecialUse
    protected static final SimpleType<String> STRING = SimpleType.STRING;
    @SpecialUse
    protected static final SimpleType<Boolean> BOOL = SimpleType.BOOLEAN;
    @SpecialUse
    protected static final SimpleType<ObjectName> OBJECTNAME = SimpleType.OBJECTNAME;
    @SpecialUse
    protected static final SimpleType<BigInteger> BIGINT = SimpleType.BIGINTEGER;
    @SpecialUse
    protected static final SimpleType<BigDecimal> BIGDECIMAL = SimpleType.BIGDECIMAL;
    @SpecialUse
    protected static final SimpleType<Character> CHAR = SimpleType.CHARACTER;

    private OpenType<?> openType = STRING;

    private static String getDescription(final Map item,
                                         final String fallback) {
        return item.containsKey(ITEM_DESCR) ?
                Objects.toString(item.get(ITEM_DESCR)) : fallback;
    }

    private static OpenType<?> getType(final Map item) throws OpenDataException{
        final Object result = item.get(ITEM_TYPE);
        if(result instanceof OpenType<?>)
            return (OpenType<?>)result;
        else throw new OpenDataException("Item type is not declared");
    }

    private static boolean isIndexed(final Map column){
        if(column.containsKey(ITEM_INDEXED)) {
            final Object indexed = column.get(ITEM_INDEXED);
            if (indexed instanceof Boolean)
                return (Boolean) indexed;
            else if (indexed instanceof String)
                return ((String) indexed).length() > 0;
            else return indexed != null;
        }
        else return false;
    }

    //<editor-fold desc="Script helpers">

    /**
     * Declares dictionary type.
     * @param typeName Dictionary type name.
     * @param typeDescription Type description.
     * @param items Definition of dictionary items.
     * @return Dictionary type definition.
     * @throws OpenDataException Invalid type definition.
     */
    @SpecialUse
    protected static CompositeType DICTIONARY(final String typeName,
                                                final String typeDescription,
                                                final Map<String, ?> items) throws OpenDataException {

        final CompositeTypeBuilder builder = new CompositeTypeBuilder(typeName, typeDescription);
        for (final Map.Entry<String, ?> item : items.entrySet())
            if (item.getValue() instanceof Map) {
                final String itemName = item.getKey();
                final String itemDescription = getDescription((Map) item.getValue(), itemName);
                final OpenType<?> itemType = getType((Map) item.getValue());
                builder.addItem(itemName, itemDescription, itemType);
            }
        return builder.build();
    }

    private static CompositeData asDictionary(final CompositeType type,
                                              final Map<String, ?> items) throws OpenDataException{
        return new CompositeDataSupport(type, items);
    }

    @SpecialUse
    protected final CompositeData asDictionary(final Map<String, ?> items) throws OpenDataException{
        final OpenType<?> t = type();
        if(t instanceof CompositeType)
            return asDictionary((CompositeType)t, items);
        else throw new OpenDataException(String.format("Expected dictionary type but '%s' found", t));
    }

    private static TabularData asTable(final TabularType type,
                                       final Collection<Map<String, ?>> rows) throws OpenDataException{
        final TabularDataSupport result = new TabularDataSupport(type, rows.size() + 5, 0.75f);
        for(final Map<String, ?> row: rows)
            result.put(asDictionary(type.getRowType(), row));
        return result;
    }

    @SpecialUse
    protected final TabularData asTable(final Collection<Map<String, ?>> rows) throws OpenDataException{
        final OpenType<?> t = type();
        if(t instanceof TabularType)
            return asTable((TabularType) t, rows);
        else throw new OpenDataException(String.format("Expected dictionary type but '%s' found", t));
    }

    /**
     * Declares table type.
     * @param typeName Table type name.
     * @param typeDescription Type description.
     * @param columns Columns definition.
     * @return Table type definition.
     * @throws OpenDataException Invalid type definition.
     */
    @SpecialUse
    protected static TabularType TABLE(final String typeName,
                                      final String typeDescription,
                                      final Map<String, ?> columns) throws OpenDataException{
        final TabularTypeBuilder builder = new TabularTypeBuilder(typeName, typeDescription);
        for (final Map.Entry<String, ?> column : columns.entrySet())
            if (column.getValue() instanceof Map) {
                final String columnName = column.getKey();
                final String columnDescr = getDescription((Map) column.getValue(), columnName);
                final OpenType<?> columnType = getType((Map) column.getValue());
                final boolean indexed = isIndexed((Map)column.getValue());
                builder.addColumn(columnName, columnDescr, columnType, indexed);
            }
        return builder.build();
    }

    /**
     * Sets type of this attribute.
     * @param value The type of this attribute
     */
    @SpecialUse
    protected final void type(final OpenType<?> value){
        this.openType = Objects.requireNonNull(value);
    }

    /**
     * Gets value of this attribute.
     * @return The value of this attribute.
     * @throws Exception Unable to get attribute value.
     */
    @SpecialUse
    public Object getValue() throws Exception{
        throw new UnsupportedOperationException();
    }

    /**
     * Sets value of this attribute.
     * @param value The value of this attribute.
     * @return A new attribute value.
     * @throws Exception Unable to set attribute value.
     */
    @SpecialUse
    public Object setValue(final Object value) throws Exception{
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether this attribute is readable.
     * @return {@literal true}, if this method is readable.
     */
    @SpecialUse
    public final boolean isReadable(){
        try {
            final Method getter = getClass().getMethod(GET_VALUE_METHOD);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        }
        catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Determines whether this attribute is writable.
     * @return {@literal true}, if this method is writable.
     */
    @SpecialUse
    public final boolean isWritable(){
        try {
            final Method getter = getClass().getMethod(SET_VALUE_METHOD, Object.class);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        }
        catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * Releases all resources associated with this attribute.
     * @throws Exception Releases all resources associated with this attribute.
     */
    @Override
    @SpecialUse
    public void close() throws Exception {
        openType = null;
    }

    //</editor-fold>

    //<editor-fold desc="Internal operations">

    /**
     * Gets type of this attribute.
     * @return The type of this attribute.
     */
    public final OpenType<?> type(){
        return openType;
    }

    public final AttributeSpecifier specifier() {
        return AttributeSpecifier
                .NOT_ACCESSIBLE
                .writable(isWritable())
                .readable(isReadable());
    }

    //</editor-fold>
}
