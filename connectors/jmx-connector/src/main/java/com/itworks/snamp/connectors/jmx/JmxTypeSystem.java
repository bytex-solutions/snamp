package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.*;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.WellKnownTypeSystem;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents JMX type system. This class cannot be inherited.
 * @author Roman Sakno
 */
final class JmxTypeSystem extends WellKnownTypeSystem {
    static final TypeToken<TabularData> TABULAR_DATA = TypeToken.of(TabularData.class);
    static final TypeToken<CompositeData> COMPOSITE_DATA = TypeToken.of(CompositeData.class);

    private static final Logger log = JmxConnectorHelpers.getLogger();

    /**
     * Initializes a new builder of JMX managementAttributes.
     */
    public JmxTypeSystem(){
        registerConverter(TABULAR_DATA, TypeLiterals.STRING_COLUMN_TABLE,
                new Function<TabularData, Table<String>>() {
                    @Override
                    public Table<String> apply(final TabularData input) {
                        return convertToTable(input);
                    }
                });
        registerConverter(COMPOSITE_DATA, TypeLiterals.STRING_MAP,
                new Function<CompositeData, Map<String,Object>>() {
                    @Override
                    public Map<String, Object> apply(final CompositeData input) {
                        return convertToMap(input);
                    }
                });
        registerConverter(COMPOSITE_DATA, TypeLiterals.STRING_COLUMN_TABLE,
                new Function<CompositeData, Table<String>>() {
                    @Override
                    public Table<String> apply(final CompositeData input) {
                        return convertToTable(input);
                    }
                });
    }

    /**
     * Converts the JMX table into well-known table.
     * @param value JMX table to convert.
     * @return Well-known representation of the JMX table.
     */
    private static Table<String> convertToTable(final TabularData value){
        final TabularType tt = value.getTabularType();
        final InMemoryTable<String> result = new InMemoryTable<>(tt.getRowType().keySet(),
                Object.class,
                value.size());
        for(final Object rowIndex: value.values()){
            final CompositeData nativeRow = (CompositeData)rowIndex;
            final Map<String, Object> tableRow = result.newRow();
            for(final String columnName: tt.getRowType().keySet())
                tableRow.put(columnName, nativeRow.get(columnName));
            result.addRow(tableRow);
        }
        return result;
    }

    private static Map<String, Object> convertToMap(final CompositeData value){
        final CompositeType ct = value.getCompositeType();
        final Map<String, Object> result = new HashMap<>(ct.keySet().size());
        for(final String key: ct.keySet())
            result.put(key, value.get(key));
        return result;
    }

    private static Table<String> convertToTable(final CompositeData value) {
        final CompositeType ct = value.getCompositeType();
        final InMemoryTable<String> result = new InMemoryTable<>(ct.keySet(), Object.class, 1);
        final Map<String, Object> row = result.newRow();
        for (final String columnName : ct.keySet())
            row.put(columnName, value.get(columnName));
        result.addRow(row);
        return result;
    }

    private static OpenType<?> getOpenType(final String typeName, final Supplier<OpenType<?>> fallback){
        try{
            switch (typeName){
                case "byte":
                case "java.lang.Byte": return SimpleType.BYTE;
                case "short":
                case "java.lang.Short": return SimpleType.SHORT;
                case "int":
                case "java.lang.Integer": return SimpleType.INTEGER;
                case "long":
                case "java.lang.Long": return SimpleType.LONG;
                case "java.lang.String": return SimpleType.STRING;
                case "java.lang.Date": return (SimpleType.DATE);
                case "float":
                case "java.lang.Float": return (SimpleType.FLOAT);
                case "double":
                case "java.lang.Double": return (SimpleType.DOUBLE);
                case "char":
                case "java.lang.Character": return (SimpleType.CHARACTER);
                case "boolean":
                case "java.lang.Boolean": return (SimpleType.BOOLEAN);
                case "java.math.BigInteger": return (SimpleType.BIGINTEGER);
                case "java.math.BigDecimal": return (SimpleType.BIGDECIMAL);
                case "byte[]":
                case "java.lang.Byte[]": return (new ArrayType<Byte[]>(1, SimpleType.BYTE));
                case "short[]":
                case "java.lang.Short[]": return (new ArrayType<Short[]>(1, SimpleType.SHORT));
                case "int[]":
                case "java.lang.Integer[]": return (new ArrayType<Integer[]>(1, SimpleType.INTEGER));
                case "long[]":
                case "java.lang.Long[]": return (new ArrayType<Long[]>(1, SimpleType.SHORT));
                case "java.lang.String[]": return (new ArrayType<String[]>(1, SimpleType.STRING));
                case "java.lang.Date[]": return (new ArrayType<Date[]>(1, SimpleType.DATE));
                case "float[]":
                case "java.lang.Float[]": return (new ArrayType<Float[]>(1, SimpleType.FLOAT));
                case "double[]":
                case "java.lang.Double[]": return (new ArrayType<Double[]>(1, SimpleType.DOUBLE));
                case "char[]":
                case "java.lang.Character[]": return (new ArrayType<Character[]>(1, SimpleType.CHARACTER));
                case "boolean[]":
                case "java.lang.Boolean[]": return (new ArrayType<Boolean[]>(1, SimpleType.BOOLEAN));
                case "java.math.BigInteger[]": return (new ArrayType<BigInteger[]>(1, SimpleType.BIGINTEGER));
                case "java.math.BigDecimal[]": return (new ArrayType<BigDecimal[]>(1, SimpleType.BIGDECIMAL));
                default: return fallback != null ? fallback.get() : SimpleType.STRING ;
            }
        }
        catch(final OpenDataException e){
            return fallback != null ? fallback.get() : SimpleType.STRING;
        }
    }

    public static OpenType<?> getOpenType(final MBeanAttributeInfo targetAttr, final Supplier<OpenType<?>> fallback) {
        if(targetAttr instanceof OpenMBeanAttributeInfo)
             return ((OpenMBeanAttributeInfo)targetAttr).getOpenType();
        else if(ArrayUtils.contains(targetAttr.getDescriptor().getFieldNames(), "openType"))
            return (OpenType<?>)targetAttr.getDescriptor().getFieldValue("openType");
        else return getOpenType(targetAttr.getType(), fallback);
    }

    /**
     * Represents MBean entity that supports MBean Open Type.
     * @param <E> Type of the instance described by MBean Open Type.
     * @param <T> MBean Open Type.
     */
    private static abstract class AbstractJmxEntityType<E, T extends OpenType<E>> extends AbstractManagedEntityType implements JmxManagedEntityOpenType<E> {
        /**
         * Represents MBean Open Type associated with this JMX entity.
         */
        private final T openType;

        /**
         * Initializes a new instance of MBean entity type.
         * @param ot Simple JMX type descriptor. Cannot be {@literal null}.
         * @param converters Additional converters associated with JMX type.
         * @throws IllegalArgumentException {@code st} is {@literal null}.
         */
        protected AbstractJmxEntityType(final T ot, final TypeConverter<?>... converters){
            super(converters);
            if(ot == null) throw new IllegalArgumentException("ot is null.");
            openType = ot;
        }

        /**
         * Returns Open MBean type associated with this management entity type.
         *
         * @return An open type descriptor.
         */
        @Override
        public final T getOpenType() {
            return openType;
        }
    }

    /**
     * Represents simple Open MBean type.
     * This class cannot be inherited.
     * @param <T> Underlying Java simple type.
     */
    private static final class JmxSimpleEntityType<T> extends AbstractJmxEntityType<T, SimpleType<T>>{

        /**
         * Initializes a new simple JMX entity type based on open MBean simple type.
         * @param st Simple JMX type descriptor. Cannot be {@literal null}.
         * @throws IllegalArgumentException {@code st} is {@literal null}.
         */
        public JmxSimpleEntityType(final SimpleType<T> st){
            super(st);
        }

        public static <T> Supplier<JmxSimpleEntityType<T>> createActivator(final SimpleType<T> simpleType){
            return new Supplier<JmxSimpleEntityType<T>>() {
                @Override
                public JmxSimpleEntityType<T> get() {
                    return new JmxSimpleEntityType<>(simpleType);
                }
            };
        }

        private static Character convertToJmxType(final String value){
            return value != null && value.length() > 0 ? value.charAt(0) : '\0';
        }

        /**
         * Converts well-known management entity value into JMX-specific value.
         *
         * @param value The value to convert into well-known JMX type.
         * @return The converted JMX-compliant value.
         */
        @Override
        public final Object convertToJmx(final Object value) {
            //BANANA: character is represented as string and should be converted into native Character from its string representation
            return getOpenType() == SimpleType.CHARACTER && value instanceof String ?
                    convertToJmxType(Objects.toString(value, "")):
                    value;
        }
    }

    private static abstract class AbstractJmxEntityTabularType extends AbstractJmxEntityType<TabularData, TabularType> implements JmxManagedEntityOpenType<TabularData>, ManagedEntityTabularType {
        public static final TypeToken<Table<String>> WELL_KNOWN_TYPE = TypeLiterals.STRING_COLUMN_TABLE;

        protected AbstractJmxEntityTabularType(final TabularType ttype){
            super(ttype);
        }

        /**
         * Determines whether the specified column is indexed.
         *
         * @param column The name of the column.
         * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
         */
        @Override
        public final boolean isIndexed(final String column) {
            return getOpenType().getIndexNames().contains(column);
        }

        /**
         * Returns a set of column names.
         *
         * @return The set of column names.
         */
        @Override
        public final Collection<String> getColumns() {
            return getOpenType().getRowType().keySet();
        }

        /**
         * Returns the number of rows if this information is available.
         *
         * @return The count of rows.
         * @throws UnsupportedOperationException Row count is not supported.
         */
        @Override
        public final long getRowCount() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("JMX composite type has no information about row count.");
        }
    }

    private final class JmxManagedEntityTabularType extends AbstractJmxEntityTabularType{

        public JmxManagedEntityTabularType(final TabularType ttype){
            super(ttype);
        }

        private TabularData convertToJmxType(final Table<String> value) throws InvalidAttributeValueException{
            final CompositeData[] rows = new CompositeData[value.getRowCount()];
            final CompositeType columnSchema = getOpenType().getRowType();
            try{
                //fills the rows
                for(int rowIndex = 0; rowIndex < value.getRowCount(); rowIndex++){
                    final int rowID = rowIndex;
                    //create each cell in the row
                    rows[rowIndex] = new CompositeDataSupport(columnSchema, new HashMap<String, Object>(){{
                        for(final String columnName: columnSchema.keySet()){
                            final JmxManagedEntityType columnType = createEntityType(columnSchema.getType(columnName));
                            put(columnName, columnType.convertToJmx(value.getCell(columnName, rowID)));
                        }
                    }});
                }
            }
            catch (final OpenDataException e){
                throw new InvalidAttributeValueException(e.toString());
            }
            final TabularData result = new TabularDataSupport(getOpenType());
            result.putAll(rows);
            return result;
        }

        /**
         * Returns the column type.
         *
         * @param column The name of the column.
         * @return The type of the column; or {@literal null} if the specified column doesn't exist.
         */
        @Override
        public final ManagedEntityType getColumnType(final String column) {
            return createEntityType(getOpenType().getRowType().getType(column));
        }

        private TabularData convertToJmxType(final TabularData value) throws OpenDataException{
            final TabularData result = new TabularDataSupport(getOpenType());
            for(final Object row: value.values())
                if(row instanceof CompositeData){
                    final CompositeData newRow = new CompositeDataSupport(getOpenType().getRowType(),
                            ArrayUtils.toArray(((CompositeData)row).getCompositeType().keySet(), String.class),
                            ((CompositeData)row).values().toArray());
                    result.put(newRow);
                }
            return result;
        }

        /**
         * Converts well-known management entity value into JMX-specific value.
         *
         * @param value The value to convert into JMX tabular data.
         * @return JMX-compliant tabular representation of the specified object.
         */
        @Override
        public final TabularData convertToJmx(final Object value) throws InvalidAttributeValueException{
            if(TypeLiterals.isInstance(value, WELL_KNOWN_TYPE))
                return convertToJmxType(TypeLiterals.cast(value, WELL_KNOWN_TYPE));
            else if(value instanceof TabularData)
                try {
                    return convertToJmxType((TabularData)value);
                }
                catch (final OpenDataException e) {
                    throw new InvalidAttributeValueException(e.toString());
                }
            else throw new InvalidAttributeValueException(String.format("Cannot convert %s to tabular data.", value));
        }
    }

    private static abstract class AbstractJmxEntityCompositeType extends AbstractJmxEntityType<CompositeData, CompositeType> implements JmxManagedEntityOpenType<CompositeData>, ManagedEntityTabularType {
        public static final TypeToken<?>[] WELL_KNOWN_TYPES = new TypeToken<?>[]{
                TypeLiterals.STRING_MAP,
                TypeLiterals.STRING_COLUMN_TABLE
        };

        protected AbstractJmxEntityCompositeType(final CompositeType ctype){
            super(ctype);
        }

        /**
         * Determines whether the specified column is indexed.
         *
         * @param column The name of the column.
         * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
         */
        @Override
        public final boolean isIndexed(final String column) {
            return false;
        }

        /**
         * Returns a set of column names.
         *
         * @return The set of column names.
         */
        @Override
        public final Collection<String> getColumns() {
            return getOpenType().keySet();
        }

        /**
         * Returns the number of rows if this information is available.
         *
         * @return The count of rows.
         * @throws UnsupportedOperationException Row count is not supported.
         */
        @Override
        public final long getRowCount() {
            return 1L;
        }
    }

    private final class JmxManagedEntityCompositeType extends AbstractJmxEntityCompositeType{
        public JmxManagedEntityCompositeType(final CompositeType ctype){
            super(ctype);
        }

        private CompositeData convertToJmxType(final Map<String, Object> value) throws InvalidAttributeValueException{
            final Map<String, Object> convertedValue = new HashMap<>(value.size());
            for(final String columnName: value.keySet()){
                final JmxManagedEntityType columnType = createEntityType(getOpenType().getType(columnName));
                convertedValue.put(columnName, columnType.convertToJmx(value.get(columnName)));
            }
            try {
                return new CompositeDataSupport(getOpenType(), convertedValue);
            }
            catch (final OpenDataException e) {
                throw new InvalidAttributeValueException(e.toString());
            }
        }

        private CompositeData convertToJmxType(final Table<String> value) throws InvalidAttributeValueException{
            final Map<String, Object> result = new HashMap<>(10);
            if(value.getRowCount() > 0)
                for(final String columnName: getOpenType().keySet()){
                    final JmxManagedEntityType columnType = createEntityType(getOpenType().getType(columnName));
                    result.put(columnName, columnType.convertToJmx(value.getCell(columnName, 0)));
                }
            else throw new InvalidAttributeValueException(String.format("Cannot convert %s to composite data.", value));
            try{
                return new CompositeDataSupport(getOpenType(), result);
            }
            catch (final OpenDataException e){
                throw new InvalidAttributeValueException(e.getMessage());
            }
        }

        private CompositeData convertToJmxType(final CompositeData value) throws OpenDataException{
            return new CompositeDataSupport(getOpenType(),
                    ArrayUtils.toArray(value.getCompositeType().keySet(), String.class),
                    value.values().toArray());
        }

        /**
         * Converts well-known management entity value into JMX-specific value.
         *
         * @param value The value to convert into JMX composite data.
         * @return JMX-compliant dictionary representation of the specified object.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final CompositeData convertToJmx(final Object value) throws InvalidAttributeValueException{
            if(TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                return convertToJmxType(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE));
            else if(TypeLiterals.isInstance(value, TypeLiterals.STRING_MAP))
                return convertToJmxType(TypeLiterals.cast(value, TypeLiterals.STRING_MAP));
            else if(value instanceof CompositeData)
                try {
                    return convertToJmxType((CompositeData)value);
                }
                catch (final OpenDataException e) {
                    throw new InvalidAttributeValueException(e.toString());
                }
            else throw new InvalidAttributeValueException(String.format("Cannot convert %s to composite data.", value));
        }

        /**
         * Returns the column type.
         *
         * @param column The name of the column.
         * @return The type of the column; or {@literal null} if the specified column doesn't exist.
         */
        @Override
        public final ManagedEntityType getColumnType(final String column) {
            return createEntityType(getOpenType().getType(column));
        }
    }

    private abstract static class AbstractJmxEntityArrayType<T> extends AbstractJmxEntityType<T[], ArrayType<T[]>> implements JmxManagedEntityOpenType<T[]>, ManagedEntityTabularType {
        public static final TypeToken<Object[]> WELL_KNOWN_TYPE = TypeLiterals.OBJECT_ARRAY;

        private static final class ArrayConverter implements TypeConverter<Object[]>{

            @Override
            public final TypeToken<Object[]> getType() {
                return TypeLiterals.OBJECT_ARRAY;
            }

            @Override
            public final boolean canConvertFrom(final TypeToken<?> source) {
                return source != null && source.isArray();
            }

            @Override
            public final Object[] convertFrom(final Object value) throws IllegalArgumentException {
                if(value != null && value.getClass().isArray()){
                    final Object[] result = new Object[Array.getLength(value)];
                    for(int i = 0; i < result.length; i++)
                        result[i] = Array.get(value, i);
                    return result;
                }
                else return new Object[]{value};
            }
        }

        protected AbstractJmxEntityArrayType(final ArrayType<T[]> atype){
            super(atype, new ArrayConverter());
        }

        @SuppressWarnings("UnusedDeclaration")
        public abstract JmxManagedEntityType getElementType();

        /**
         * Determines whether the specified column is indexed.
         *
         * @param column The name of the column.
         * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
         */
        @Override
        public final boolean isIndexed(final String column) {
            return ManagedEntityArrayType.isIndexedColumn(column);
        }
    }

    private final class JmxManagedEntityArrayType<T> extends AbstractJmxEntityArrayType<T>{

        public JmxManagedEntityArrayType(final ArrayType<T[]> atype) throws OpenDataException {
            super(atype);
        }

        @Override
        public final JmxManagedEntityType getElementType(){
            return createEntityType(getOpenType().getElementOpenType());
        }

        private Object convertToJmxArray(final Object value){
            try {
                final Class<?> elementType = Class.forName(getOpenType().getElementOpenType().getClassName());
                final Object result = Array.newInstance(elementType, Array.getLength(value));
                for(int i = 0; i < Array.getLength(value); i++)
                    Array.set(result, i, Array.get(value, i));
                return result;
            }
            catch (final ClassNotFoundException e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                return value;
            }
        }

        /**
         * Converts well-known management entity value into JMX-specific value.
         *
         * @param value The value to convert into well-known JMX array type.
         * @return JMX-compliant array.
         */
        @Override
        public final Object convertToJmx(final Object value) {
            if(value != null && value.getClass().isArray())
                return convertToJmxArray(value);
            else return convertToJmxArray(new Object[]{value});
        }

        /**
         * Returns a set of column names.
         *
         * @return The set of column names.
         */
        @Override
        public final Collection<String> getColumns() {
            return Arrays.asList(ManagedEntityArrayType.INDEX_COLUMN_NAME,
                    ManagedEntityArrayType.VALUE_COLUMN_NAME);
        }

        /**
         * Returns the column type.
         *
         * @param column The name of the column.
         * @return The type of the column; or {@literal null} if the specified column doesn't exist.
         */
        @Override
        public final JmxManagedEntityType getColumnType(final String column) {
            switch (column){
                case ManagedEntityArrayType.INDEX_COLUMN_NAME: return createEntityType(SimpleType.INTEGER);
                case ManagedEntityArrayType.VALUE_COLUMN_NAME: return getElementType();
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
        public final long getRowCount() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("JMX array type doesn't provide element count.");
        }
    }

    private  <T> JmxManagedEntityOpenType<T> createEntitySimpleType(final SimpleType<T> attributeType, final TypeToken<T> classRef){
        return createEntityType(JmxSimpleEntityType.createActivator(attributeType), classRef);
    }

    private JmxManagedEntityOpenType<?> createEntitySimpleType(final SimpleType<?> attributeType){
        if(attributeType == SimpleType.BOOLEAN)
            return createEntitySimpleType(SimpleType.BOOLEAN, TypeLiterals.BOOLEAN);
        else if(attributeType == SimpleType.BIGDECIMAL)
            return createEntitySimpleType(SimpleType.BIGDECIMAL, TypeLiterals.BIG_DECIMAL);
        else if(attributeType == SimpleType.BIGINTEGER)
            return createEntitySimpleType(SimpleType.BIGINTEGER, TypeLiterals.BIG_INTEGER);
        else if(attributeType == SimpleType.BYTE)
            return createEntitySimpleType(SimpleType.BYTE, TypeLiterals.BYTE);
        else if(attributeType == SimpleType.INTEGER)
            return createEntitySimpleType(SimpleType.INTEGER, TypeLiterals.INTEGER);
        else if(attributeType == SimpleType.CHARACTER)
            return createEntityType(JmxSimpleEntityType.createActivator(SimpleType.CHARACTER), TypeLiterals.STRING);
        else if(attributeType == SimpleType.STRING)
            return createEntitySimpleType(SimpleType.STRING, TypeLiterals.STRING);
        else if(attributeType == SimpleType.DATE)
            return createEntitySimpleType(SimpleType.DATE, TypeLiterals.DATE);
        else if(attributeType == SimpleType.DOUBLE)
            return createEntitySimpleType(SimpleType.DOUBLE, TypeLiterals.DOUBLE);
        else if(attributeType == SimpleType.FLOAT)
            return createEntitySimpleType(SimpleType.FLOAT, TypeLiterals.FLOAT);
        else if(attributeType == SimpleType.SHORT)
            return createEntitySimpleType(SimpleType.SHORT, TypeLiterals.SHORT);
        else if(attributeType == SimpleType.LONG)
            return createEntitySimpleType(SimpleType.LONG, TypeLiterals.LONG);
        else return createEntitySimpleType(SimpleType.STRING, TypeLiterals.STRING);
    }

    private <T> AbstractJmxEntityArrayType<T> createEntityArrayType(final ArrayType<T[]> attributeType){
        return createEntityType(new Supplier<AbstractJmxEntityArrayType<T>>(){
            @Override
            public final AbstractJmxEntityArrayType<T> get() {
                try {
                    return new JmxManagedEntityArrayType<>(attributeType);
                }
                catch (final OpenDataException e) {
                    log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    return null;
                }
            }
        }, AbstractJmxEntityArrayType.WELL_KNOWN_TYPE);
    }

    @SuppressWarnings("unchecked")
    public final JmxManagedEntityOpenType<?> createEntityType(final OpenType<?> attributeType){
        if(attributeType instanceof SimpleType)
            return createEntitySimpleType((SimpleType<?>) attributeType);
        else if(attributeType instanceof CompositeType)
            return createEntityType(new Supplier<AbstractJmxEntityCompositeType>(){
                @Override
                public final AbstractJmxEntityCompositeType get() {
                    return new JmxManagedEntityCompositeType((CompositeType)attributeType);
                }
            }, AbstractJmxEntityCompositeType.WELL_KNOWN_TYPES);
        else if(attributeType instanceof ArrayType)
            return createEntityArrayType((ArrayType) attributeType);
        else if(attributeType instanceof TabularType)
            return createEntityType(new Supplier<AbstractJmxEntityTabularType>(){
                @Override
                public AbstractJmxEntityTabularType get() {
                    return new JmxManagedEntityTabularType((TabularType)attributeType);
                }
            }, AbstractJmxEntityTabularType.WELL_KNOWN_TYPE);
        else return createEntitySimpleType(SimpleType.STRING);
    }

    public JmxManagedEntityType createEntityType(final Class<?> attributeType, final Supplier<OpenType<?>> fallback){
        return createEntityType(attributeType.getCanonicalName(), fallback);
    }

    public JmxManagedEntityType createEntityType(final String attributeType, final Supplier<OpenType<?>> fallback){
        return createEntityType(getOpenType(attributeType, fallback));
    }

    public static OpenType<?> getOpenTypeFromValue(final Object value){
        if(value instanceof CompositeData)
            return ((CompositeData)value).getCompositeType();
        else if(value instanceof TabularData)
            return ((TabularData)value).getTabularType();
        else if(value != null)
            return getOpenType(value.getClass().getCanonicalName(), null);
        else return SimpleType.STRING;
    }

    public JmxManagedEntityType createAttributeType(final MBeanAttributeInfo attribute, final Supplier<OpenType<?>> fallback){
        return createEntityType(getOpenType(attribute, fallback));
    }
}
