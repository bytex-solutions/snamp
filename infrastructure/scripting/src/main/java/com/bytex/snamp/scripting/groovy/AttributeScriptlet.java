package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.TabularTypeBuilder;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import static com.bytex.snamp.scripting.groovy.OpenDataScriptHelpers.*;

/**
 * Represents interface for base script class represents attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface AttributeScriptlet extends ScriptingAPI {
    @SpecialUse
    SimpleType<Byte> INT8 = SimpleType.BYTE;
    @SpecialUse
    SimpleType<Short> INT16 = SimpleType.SHORT;
    @SpecialUse
    SimpleType<Integer> INT32 = SimpleType.INTEGER;
    @SpecialUse
    SimpleType<Long> INT64 = SimpleType.LONG;
    @SpecialUse
    SimpleType<Float> FLOAT32 = SimpleType.FLOAT;
    @SpecialUse
    SimpleType<Double> FLOAT64 = SimpleType.DOUBLE;
    @SpecialUse
    SimpleType<String> STRING = SimpleType.STRING;
    @SpecialUse
    SimpleType<Boolean> BOOL = SimpleType.BOOLEAN;
    @SpecialUse
    SimpleType<ObjectName> OBJECTNAME = SimpleType.OBJECTNAME;
    @SpecialUse
    SimpleType<BigInteger> BIGINT = SimpleType.BIGINTEGER;
    @SpecialUse
    SimpleType<BigDecimal> BIGDECIMAL = SimpleType.BIGDECIMAL;
    @SpecialUse
    SimpleType<Character> CHAR = SimpleType.CHARACTER;
    @SpecialUse
    SimpleType<Date> DATETIME = SimpleType.DATE;
    @SpecialUse
    static <T> ArrayType<T[]> ARRAY(final OpenType<T> elementType) throws OpenDataException {
        return ArrayUtils.createArrayType(elementType);
    }
    /**
     * Declares dictionary type.
     *
     * @param typeName        Dictionary type name.
     * @param typeDescription Type description.
     * @param items           Definition of dictionary items.
     * @return Dictionary type definition.
     * @throws OpenDataException Invalid type definition.
     */
    @SpecialUse
    static CompositeType DICTIONARY(final String typeName,
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
    /**
     * Declares table type.
     *
     * @param typeName        Table type name.
     * @param typeDescription Type description.
     * @param columns         Columns definition.
     * @return Table type definition.
     * @throws OpenDataException Invalid type definition.
     */
    @SpecialUse
    static TabularType TABLE(final String typeName,
                                       final String typeDescription,
                                       final Map<String, ?> columns) throws OpenDataException {
        final TabularTypeBuilder builder = new TabularTypeBuilder(typeName, typeDescription);
        for (final Map.Entry<String, ?> column : columns.entrySet())
            if (column.getValue() instanceof Map) {
                final String columnName = column.getKey();
                final String columnDescr = getDescription((Map) column.getValue(), columnName);
                final OpenType<?> columnType = getType((Map) column.getValue());
                final boolean indexed = isIndexed((Map) column.getValue());
                builder.addColumn(columnName, columnDescr, columnType, indexed);
            }
        return builder.build();
    }

    /**
     * Declares attribute type.
     * @param value Attribute type.
     */
    void type(final OpenType<?> value);

    /**
     * Gets attribute type.
     * @return Type of attribute.
     */
    OpenType<?> type();

    /**
     * Converts map into composite data.
     * @param items Items of the composite data.
     * @return Composite data.
     * @throws OpenDataException Unable to create composite data.
     */
    CompositeData asDictionary(final Map<String, ?> items) throws OpenDataException;

    TabularData asTable(final Collection<Map<String, ?>> rows) throws OpenDataException;

    static CompositeData asDictionary(final CompositeType type,
                                      final Map<String, ?> items) throws OpenDataException {
        return new CompositeDataSupport(type, items);
    }

    static TabularData asTable(final TabularType type,
                                       final Collection<Map<String, ?>> rows) throws OpenDataException {
        final TabularDataSupport result = new TabularDataSupport(type, rows.size() + 5, 0.75f);
        for (final Map<String, ?> row : rows)
            result.put(asDictionary(type.getRowType(), row));
        return result;
    }
}