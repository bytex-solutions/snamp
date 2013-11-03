package com.snamp.connectors;

import com.snamp.SimpleTable;
import com.snamp.Table;

import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.*;
import java.util.*;

/**
 * Represents JMX attribute type information.
 * @author roman
 */
final class JmxAttributeTypeInfoBuilder extends AttributePrimitiveTypeBuilder {
    /**
     * Initializes a new builder of JMX attributes.
     */
    public JmxAttributeTypeInfoBuilder(){

    }

    private static interface AttributeJmxTabularType extends AttributeConvertibleTypeInfo<TabularData>, AttributeTabularType{

    }

    private static AttributeJmxTabularType createJmxTabularType(final TabularType tt){
        return new AttributeJmxTabularType() {
            @Override
            public Class<TabularData> getNativeClass() {
                return TabularData.class;
            }

            @Override
            public Set<String> getColumns() {
                return tt.getRowType().keySet();
            }

            @Override
            public AttributeTypeInfo getColumnType(final String column) {
                return createJmxType(tt.getRowType().getType(column));
            }

            @Override
            public long getRowCount() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("The row count is unknown.");
            }

            @Override
            public final  <T> boolean canConvertTo(final Class<T> target) {
                return target == Table.class;
            }

            private SimpleTable<String> convertToTable(final TabularData value){
                final SimpleTable<String> result = new SimpleTable<>(new HashMap<String, Class<?>>(){{
                    final CompositeType rowType = tt.getRowType();
                    for(final String columnName: rowType.keySet())
                        put(columnName, Object.class);
                }});
                for(final Object rowIndex: value.keySet()){
                    final CompositeData nativeRow = value.get((Object[])rowIndex);
                    final Map<String, Object> tableRow = new HashMap<>(10);
                    for(final String columnName: tt.getRowType().keySet())
                        tableRow.put(columnName, nativeRow.get(columnName));
                    result.addRow(tableRow);
                }
                return result;
            }

            private Map<String, Object>[] convertToMapArray(final TabularData value){
                final SimpleTable<String> result = convertToTable(value);
                return result.toArray();
            }

            private final  <T> T convertTo(final TabularData value, final Class<T> target) throws IllegalArgumentException{
                if(Table.class == target)
                    return (T)convertToTable(value);
                else if(Map[].class == target)
                    return (T)convertToMapArray(value);
                else throw new IllegalArgumentException(String.format("Cannot convert %s value to table.", value));
            }

            @Override
            public final <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
                if(value instanceof TabularData)
                    return convertTo((TabularData)value, target);
                else throw new IllegalArgumentException(String.format("Cannot convert %s value to table.", value));
            }

            @Override
            public final <T> boolean canConvertFrom(final Class<T> source) throws IllegalArgumentException{
                return TabularData.class == source || Map[].class == source || Table.class == source;
            }

            private TabularData convertFromTable(final Table<String> table){
                final CompositeData[] rows = new CompositeData[table.getRowCount()];
                final CompositeType columnSchema = tt.getRowType();
                try{
                    //fills the rows
                    for(int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++){
                        final int rowID = rowIndex;
                        //create each cell in the row
                        rows[rowIndex] = new CompositeDataSupport(columnSchema, new HashMap<String, Object>(){{
                            for(final String columnName: columnSchema.keySet()){
                                final AttributeConvertibleTypeInfo<?> columnType = createJmxType(columnSchema.getType(columnName));
                                put(columnName, columnType.convertFrom(table.getCell(columnName, rowID)));
                            }
                        }});
                    }
                }
                catch (final OpenDataException e){
                    throw new IllegalArgumentException(e);
                }
                final TabularData result = new TabularDataSupport(tt);
                result.putAll(rows);
                return result;
            }

            private TabularData convertFromMapArray(final Map<String, Object>[] table){
                return convertFromTable(SimpleTable.fromArray(table));
            }

            @Override
            public TabularData convertFrom(final Object value) throws IllegalArgumentException {
                if(value instanceof TabularData) return (TabularData)value;
                else if(value instanceof Map[]) return convertFromMapArray((Map<String, Object>[])value);
                else if(value instanceof Table) return convertFromTable((Table<String>)value);
                else throw new IllegalArgumentException(String.format("Cannot create table from %s", value));
            }
        };
    }

    private static interface AttributeJmxCompositeType extends AttributeConvertibleTypeInfo<CompositeData>, AttributeTabularType{

    }

    private static AttributeJmxCompositeType createJmxCompositeType(final CompositeType ct){
        return new AttributeJmxCompositeType() {
            @Override
            public final Class<CompositeData> getNativeClass() {
                return CompositeData.class;
            }

            @Override
            public final Set<String> getColumns() {
                return ct.keySet();
            }

            @Override
            public AttributeTypeInfo getColumnType(final String column) {
                return createJmxType(ct.getType(column));
            }

            @Override
            public long getRowCount() {
                return 1;
            }

            @Override
            public <T> boolean canConvertTo(final Class<T> target) {
                return Map.class == target || Table.class == target || CompositeData.class == target;
            }

            private Map<String, Object> convertToMap(final CompositeData value){
                final Map<String, Object> result = new HashMap<>(ct.keySet().size());
                for(final String key: ct.keySet())
                    result.put(key, value.get(key));
                return result;
            }

            private Table<String> convertToTable(final CompositeData value){
                final Table<String> result = new SimpleTable<>(new HashMap<String, Class<?>>(){{
                    for(final String columnName: ct.keySet())
                        put(columnName, Object.class);
                }});
                final Map<String, Object> row = new HashMap<>(ct.keySet().size());
                for(final String columnName: ct.keySet())
                    row.put(columnName, value.get(columnName));
                result.addRow(row);
                return result;
            }

            private <T> T convertTo(final CompositeData value, final Class<T> target){
                if(Map.class == target)
                    return (T)convertToMap(value);
                else if(Table.class == target)
                    return (T)convertToTable(value);
                else throw new IllegalArgumentException(String.format("Unsupported destination type %s", target));
            }

            @Override
            public <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
                if(target.isInstance(value)) return target.cast(value);
                else if(value instanceof CompositeData)
                    return convertTo((CompositeData)value, target);
                else throw new IllegalArgumentException(String.format("Unsupported %s value.", value));
            }

            @Override
            public <T> boolean canConvertFrom(final Class<T> source) {
                return Map.class == source || Table.class == source || CompositeData.class == source;
            }

            private CompositeData convertFromMap(final Map<String, Object> value) throws IllegalArgumentException{
                final Map<String, Object> convertedValue = new HashMap<>(value.size());
                for(final String columnName: value.keySet()){
                    final AttributeConvertibleTypeInfo<?> columnType = createJmxType(ct.getType(columnName));
                    convertedValue.put(columnName, columnType.convertFrom(value.get(columnName)));
                }
                try {
                    return new CompositeDataSupport(ct, convertedValue);
                }
                catch (final OpenDataException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            private CompositeData convertFromTable(final Table<String> value) throws IllegalArgumentException{
                final Map<String, Object> result = new HashMap<>(10);
                if(value.getRowCount() > 0)
                    for(final String columnName: ct.keySet()){
                        final AttributeConvertibleTypeInfo<?> columnType = createJmxType(ct.getType(columnName));
                        result.put(columnName, columnType.convertFrom(value.getCell(columnName, 0)));
                    }
                else throw new IllegalArgumentException(String.format("Cannot convert table %s to composite data.", value));
                try{
                    return new CompositeDataSupport(ct, result);
                }
                catch (final OpenDataException e){
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public final CompositeData convertFrom(final Object value) throws IllegalArgumentException {
                if(value instanceof CompositeData) return (CompositeData)value;
                else if(value instanceof Map) return convertFromMap((Map<String, Object>) value);
                else if(value instanceof Table) return convertFromTable((Table<String>)value);
                else throw new IllegalArgumentException(String.format("Value %s cannot be converted to composite data.", value));
            }
        };
    }

    private static final class AttributeJmxArrayType<T> extends AttributeArrayType implements AttributeConvertibleTypeInfo<T[]>{
        private final AttributeConvertibleTypeInfo<T> elementType;

        public AttributeJmxArrayType(final AttributeConvertibleTypeInfo<T> elementType){
            super(elementType);
            this.elementType = elementType;
        }

        /**
         * Converts the specified value into the
         *
         * @param value The value to convert.
         * @return The value of the attribute.
         */
        @Override
        public T[] convertFrom(final Object value) throws IllegalArgumentException {
            if(isArray(value) && super.elementType.canConvertFrom(value.getClass().getComponentType())){
                final Object result = Array.newInstance(elementType.getNativeClass(), Array.getLength(value));
                for(int i = 0; i < Array.getLength(value); i++)
                    Array.set(result, i, Array.get(value, i));
                return (T[])result;
            }
            else if(value instanceof Table){
                final Table<String> tbl = (Table<String>)value;
                final Object result = Array.newInstance(elementType.getNativeClass(), tbl.getRowCount());
                for(int i = 0; i < tbl.getRowCount(); i++)
                    Array.set(result, i, tbl.getCell(indexColumnName, i));
                return (T[])result;
            }
            else throw new IllegalArgumentException(String.format("Cannot convert %s value.", value));
        }

        /**
         * Returns the underlying Java class.
         *
         * @return The underlying Java class.
         */
        @Override
        public Class<T[]> getNativeClass() {
            final Object obj = Array.newInstance(elementType.getNativeClass(), 0);
            return (Class<T[]>)obj.getClass();
        }
    }

    private static AttributeConvertibleTypeInfo<?> createJmxArrayType(final ArrayType<?> attributeType){

        return new AttributeJmxArrayType(createJmxType(attributeType.getElementOpenType()));
    }

    private static AttributeConvertibleTypeInfo<?> createJmxSimpleType(final SimpleType<?> attributeType){
        if(attributeType == SimpleType.BOOLEAN)
            return createBooleanType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BIGDECIMAL)
            return createDecimalType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BIGINTEGER)
            return createIntegerType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.BYTE)
            return createInt8Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.CHARACTER || attributeType == SimpleType.STRING)
            return createStringType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.DATE)
            return createUnixTimeType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.DOUBLE)
            return createDoubleType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.FLOAT)
            return createFloatType(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.SHORT)
            return createInt16Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.LONG)
            return createInt64Type(JmxAttributeTypeInfoBuilder.class);
        else if(attributeType == SimpleType.STRING)
            return createStringType(JmxAttributeTypeInfoBuilder.class);
        else return createTypeInfo(JmxAttributeTypeInfoBuilder.class, Object.class);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final OpenType<?> attributeType){
        if(attributeType instanceof SimpleType)
            return createJmxSimpleType((SimpleType<?>)attributeType);
        else if(attributeType instanceof CompositeType)
            return createJmxCompositeType((CompositeType) attributeType);
        else if(attributeType instanceof ArrayType)
            return createJmxArrayType((ArrayType<?>) attributeType);
        else if(attributeType instanceof TabularType)
            return createJmxTabularType((TabularType)attributeType);
        else return createTypeInfo(JmxAttributeTypeInfoBuilder.class, Object.class);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final Class<?> attributeType){
        return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
    }

    public static AttributeConvertibleTypeInfo<?> createJmxType(final String attributeType){
        switch (attributeType){
            case "byte":
            case "java.lang.Byte": return createJmxType(SimpleType.BYTE);
            case "short":
            case "java.lang.Short": return createJmxType(SimpleType.SHORT);
            case "int":
            case "java.lang.Integer": return createJmxType(SimpleType.INTEGER);
            case "long":
            case "java.lang.Long": return createJmxType(SimpleType.LONG);
            case "java.lang.String": return createJmxType(SimpleType.STRING);
            case "java.lang.Date": return createJmxType(SimpleType.DATE);
            case "float":
            case "java.lang.Float": return createJmxType(SimpleType.FLOAT);
            case "double":
            case "java.lang.Double": return createJmxType(SimpleType.DOUBLE);
            case "char":
            case "java.lang.Character": return createJmxType(SimpleType.CHARACTER);
            case "boolean":
            case "java.lang.Boolean": return createJmxType(SimpleType.BOOLEAN);
            case "java.math.BigInteger": return createJmxType(SimpleType.BIGINTEGER);
            case "java.math.BigDecimal": return createJmxType(SimpleType.BIGDECIMAL);
            case "byte[]":
            case "java.lang.Byte[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Byte[].class));
            case "short[]":
            case "java.lang.Short[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Short[].class));
            case "int[]":
            case "java.lang.Integer[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Integer[].class));
            case "long[]":
            case "java.lang.Long[]": return createJmxArrayType(ArrayType.getPrimitiveArrayType(Long[].class));
            case "java.lang.String[]": return createJmxType(ArrayType.getPrimitiveArrayType(String[].class));
            case "java.lang.Date[]": return createJmxType(ArrayType.getPrimitiveArrayType(Date[].class));
            case "float[]":
            case "java.lang.Float[]": return createJmxType(ArrayType.getPrimitiveArrayType(Float[].class));
            case "double[]":
            case "java.lang.Double[]": return createJmxType(ArrayType.getPrimitiveArrayType(Double[].class));
            case "char[]":
            case "java.lang.Character[]": return createJmxType(ArrayType.getPrimitiveArrayType(Character[].class));
            case "boolean[]":
            case "java.lang.Boolean[]": return createJmxType(ArrayType.getPrimitiveArrayType(Boolean[].class));
            case "java.math.BigInteger[]": return createJmxType(ArrayType.getPrimitiveArrayType(BigInteger[].class));
            case "java.math.BigDecimal[]": return createJmxType(ArrayType.getPrimitiveArrayType(BigDecimal[].class));
            default: return createTypeInfo(JmxAttributeTypeInfoBuilder.class, attributeType);
        }
    }
}
