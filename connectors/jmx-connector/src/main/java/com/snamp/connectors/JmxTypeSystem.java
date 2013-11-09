package com.snamp.connectors;

import com.snamp.SimpleTable;
import com.snamp.Table;

import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.*;
import java.util.*;

/**
 * Represents JMX attribute type information.
 * @author Roman Sakno
 */
final class JmxTypeSystem extends WellKnownTypeSystem<EntityTypeInfoBuilder.AttributeTypeConverter> {
    /**
     * Initializes a new builder of JMX attributes.
     */
    public JmxTypeSystem(){
        super(EntityTypeInfoBuilder.AttributeTypeConverter.class);
    }

    private static interface AttributeJmxTabularType extends AttributeTypeConverter, AttributeTabularType{

    }

    private AttributeJmxTabularType createJmxTabularType(final TabularType tt){
        return new AttributeJmxTabularType() {
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
                return target != null && target.isAssignableFrom(Table.class);
            }

            private SimpleTable<String> convertToTable(final TabularData value){
                final SimpleTable<String> result = new SimpleTable<>(new HashMap<String, Class<?>>(){{
                    final CompositeType rowType = tt.getRowType();
                    for(final String columnName: rowType.keySet())
                        put(columnName, Object.class);
                }});
                for(final Object rowIndex: value.values()){
                    final CompositeData nativeRow = (CompositeData)rowIndex;
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
                if(target == null) return null;
                else if(target.isAssignableFrom(Table.class))
                    return target.cast(convertToTable(value));
                else if(target.isAssignableFrom(Map[].class))
                    return target.cast(convertToMapArray(value));
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
                return TabularData.class.isAssignableFrom(source) ||
                        Map[].class.isAssignableFrom(source) ||
                        Table.class.isAssignableFrom(source);
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
                                final AttributeTypeConverter columnType = createJmxType(columnSchema.getType(columnName));
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

    private static interface AttributeJmxCompositeType extends AttributeTypeConverter, AttributeTabularType{

    }

    private AttributeJmxCompositeType createJmxCompositeType(final CompositeType ct){
        return new AttributeJmxCompositeType() {

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
                if(target.isAssignableFrom(Map.class))
                    return target.cast(convertToMap(value));
                else if(target.isAssignableFrom(Table.class))
                    return target.cast(convertToTable(value));
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
                return Map.class.isAssignableFrom(source) ||
                        Table.class.isAssignableFrom(source) ||
                        CompositeData.class.isAssignableFrom(source);
            }

            private CompositeData convertFromMap(final Map<String, Object> value) throws IllegalArgumentException{
                final Map<String, Object> convertedValue = new HashMap<>(value.size());
                for(final String columnName: value.keySet()){
                    final AttributeTypeConverter columnType = createJmxType(ct.getType(columnName));
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
                        final AttributeTypeConverter columnType = createJmxType(ct.getType(columnName));
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

    private static final class AttributeJmxArrayType extends AttributeArrayType implements AttributeTypeConverter {
        private final AttributeTypeConverter elementType;
        private final Class<?> nativeElementType;

        public AttributeJmxArrayType(final Class<?> nativeElementType, final AttributeTypeConverter elementType){
            super(elementType);
            this.elementType = elementType;
            this.nativeElementType = nativeElementType;
        }

        /**
         * Converts the specified value into the
         *
         * @param value The value to convert.
         * @return The value of the attribute.
         */
        @Override
        public Object convertFrom(final Object value) throws IllegalArgumentException {
            if(isArray(value)){
                final Object result = Array.newInstance(nativeElementType, Array.getLength(value));
                for(int i = 0; i < Array.getLength(value); i++)
                    Array.set(result, i, Array.get(value, i));
                return result;
            }
            else if(value instanceof Table){
                final Table<String> tbl = (Table<String>)value;
                final Object result = Array.newInstance(nativeElementType, tbl.getRowCount());
                for(int i = 0; i < tbl.getRowCount(); i++)
                    Array.set(result, i, tbl.getCell(INDEX_COLUMN_NAME, i));
                return result;
            }
            else throw new IllegalArgumentException(String.format("Cannot convert %s value.", value));
        }
    }

    private AttributeTypeConverter createJmxArrayType(final ArrayType<?> attributeType){
        try {
            final Class<?> nativeElementType = Class.forName(attributeType.getElementOpenType().getClassName());
            return new AttributeJmxArrayType(nativeElementType, createJmxType(attributeType.getElementOpenType()));
        }
        catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private AttributeTypeConverter createJmxSimpleType(final SimpleType<?> attributeType){
        if(attributeType == SimpleType.BOOLEAN)
            return createBooleanType();
        else if(attributeType == SimpleType.BIGDECIMAL)
            return createDecimalType(JmxTypeSystem.class);
        else if(attributeType == SimpleType.BIGINTEGER)
            return createIntegerType();
        else if(attributeType == SimpleType.BYTE)
            return createInt8Type();
        else if(attributeType == SimpleType.INTEGER)
            return createInt32Type();
        else if(attributeType == SimpleType.CHARACTER)
            return createStringType(Character.class);
        else if(attributeType == SimpleType.STRING)
            return createStringType();
        else if(attributeType == SimpleType.DATE)
            return createUnixTimeType();
        else if(attributeType == SimpleType.DOUBLE)
            return createDoubleType();
        else if(attributeType == SimpleType.FLOAT)
            return createFloatType();
        else if(attributeType == SimpleType.SHORT)
            return createInt16Type();
        else if(attributeType == SimpleType.LONG)
            return createInt64Type();
        else return createTypeInfo(getClass(), AttributeTypeConverter.class, attributeType.getClassName());
    }

    public AttributeTypeConverter createJmxType(final OpenType<?> attributeType){
        if(attributeType instanceof SimpleType)
            return createJmxSimpleType((SimpleType<?>)attributeType);
        else if(attributeType instanceof CompositeType)
            return createJmxCompositeType((CompositeType) attributeType);
        else if(attributeType instanceof ArrayType)
            return createJmxArrayType((ArrayType<?>) attributeType);
        else if(attributeType instanceof TabularType)
            return createJmxTabularType((TabularType)attributeType);
        else return createTypeInfo(getClass(), AttributeTypeConverter.class, attributeType.getClassName());
    }

    public AttributeTypeConverter createJmxType(final Class<?> attributeType){
        return createTypeInfo(getClass(), AttributeTypeConverter.class, attributeType.getCanonicalName());
    }

    public AttributeTypeConverter createJmxType(final String attributeType){
        try{
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
                case "java.lang.Byte[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.BYTE));
                case "short[]":
                case "java.lang.Short[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.SHORT));
                case "int[]":
                case "java.lang.Integer[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.INTEGER));
                case "long[]":
                case "java.lang.Long[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.SHORT));
                case "java.lang.String[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.STRING));
                case "java.lang.Date[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.DATE));
                case "float[]":
                case "java.lang.Float[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.FLOAT));
                case "double[]":
                case "java.lang.Double[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.DOUBLE));
                case "char[]":
                case "java.lang.Character[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.CHARACTER));
                case "boolean[]":
                case "java.lang.Boolean[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.BOOLEAN));
                case "java.math.BigInteger[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.BIGINTEGER));
                case "java.math.BigDecimal[]": return createJmxArrayType(new ArrayType<>(1, SimpleType.BIGDECIMAL));
                default: return createTypeInfo(getClass(), AttributeTypeConverter.class, attributeType);
            }
        }
        catch(final OpenDataException e){
            return createTypeInfo(getClass(), AttributeTypeConverter.class, attributeType);
        }
    }
}
