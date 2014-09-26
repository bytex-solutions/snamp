package com.itworks.snamp.connectors;

import org.apache.commons.collections4.Factory;
import com.itworks.snamp.Table;

import java.math.*;
import java.util.*;

/**
 * Represents a set of well known management entity types.
 * <p>
 *     You should use this builder for all management entities which
 *     values are implicitly compatible with well-known Java types:
 *     <ul>
 *         <li>Int8 - wrapper for {@link Byte} class.</li>
 *         <li>Int16 - wrapper for {@link Short} class.</li>
 *         <li>Int32 - wrapper for {@link Integer} class.</li>
 *         <li>Int64 - wrapper for {@link Long} class.</li>
 *         <li>String - wrapper for {@link String} class.</li>
 *         <li>Integer - wrapper for {@link BigInteger} class.</li>
 *         <li>Decimal - wrapper for {@link BigDecimal} class.</li>
 *         <li>Boolean - wrapper for {@link Boolean} class.</li>
 *         <li>Date - wrapper for {@link Date} and {@link Calendar} classes both.</li>
 *     </ul>
 * </p>
 * <p>
 *     For non-scalar data types such as arrays, dictionaries and tables it is recommended to use the
 *     following mapping:
 *     <ul>
 *         <li>Tabular data should be convertible to {@link com.itworks.snamp.Table} type. An implementation
 *         for this interface is provided by {@link com.itworks.snamp.SimpleTable} class. Entity data type
 *         should implements {@link ManagedEntityTabularType} interface.</li>
 *         <li>Array data should be convertible to Java array and {@link com.itworks.snamp.Table} type. Attribute
 *         data type should inherits from {@link ManagedEntityTypeBuilder.ManagedEntityArrayType} class.</li>
 *         <li>Map data should be convertible to {@link com.itworks.snamp.Table} type, and, optionally,
 *         to {@link Map} type. Map is a special case of tabular data when table has single row
 *         and multiple columns, where each column represents map key.</li>
 *     </ul>
 * </p>
 * <p>
 * Management entity type system is a set of converters that provides conversion between MIB-specific
 * data types and universal data types. This class provides set of converters between these data types
 * in the form of static public unary methods annotated with {@link com.itworks.snamp.AbstractTypeConverterProvider.Converter} interface. Typically,
 * each custom SNAMP connector contains its own type system converter, inherited from this class.
 * The following example demonstrates your own type system converter:
 * <pre><code>
 * public final class CustomTypeInfoBuilder extends WellKnownTypeSystem{
 *
 *     {@literal @}Converter
 *     public static byte[] stringToByteArray(final String str){
 *         return str.getBytes("UTF-8");
 *     }
 *
 *     {@literal @}Converter
 *     public static String byteArrayToString(final byte[] b){
 *         return new String(b, "UTF-8");
 *     }
 *
 *     public final ManagementEntityType createByteArrayType(){
 *       return createArrayType(createInt8Type());
 *     }
 * }
 *
 * final CustomTypeInfoBuilder builder = new CustomTypeInfoBuilder();
 * final AttributeTypeInfo arrayType = builder.createByteArrayType();
 * final String result = arrayType.convertTo(new byte[]{1, 2, 3}, String.class);
 * </code></pre>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WellKnownTypeSystem extends ManagedEntityTypeBuilder {

    /**
     * Converts {@link String} to {@link Byte}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Byte stringToInt8(final String str){
        return Byte.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Short}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Short stringToInt16(final String str){
        return Short.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Integer}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Integer stringToInt32(final String str){
        return Integer.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Long}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Long stringToInt64(final String str){
        return Long.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link java.math.BigInteger}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigInteger stringToInteger(final String str){
        return new BigInteger(str);
    }

    /**
     * Converts {@link String} to {@link Boolean}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Boolean stringToBoolean(final String str){
        return Boolean.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Float}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Float stringToFloat(final String str){
        return Float.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Double}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Double stringToDouble(final String str){
        return Double.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link java.math.BigDecimal}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigDecimal stringToDecimal(final String str){
        return new BigDecimal(str);
    }

    /**
     * Converts {@link Number} to {@link Byte}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Byte numberToInt8(final Number n){
        return n.byteValue();
    }

    /**
     * Converts {@link Number} to {@link Short}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Short numberToInt16(final Number n){
        return n.shortValue();
    }

    /**
     * Converts {@link Number} to {@link Integer}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Integer numberToInt32(final Number n){
        return n.intValue();
    }

    /**
     * Converts {@link Number} to {@link Long}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Long numberToInt64(final Number n){
        return n.longValue();
    }

    /**
     * Converts {@link Number} to {@link Float}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Float numberToFloat(final Number n){
        return n.floatValue();
    }

    /**
     * Converts {@link Number} to {@link Double}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Double numberToDouble(final Number n){
        return n.doubleValue();
    }

    /**
     * Converts {@link Number} to {@link BigInteger}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigInteger numberToInteger(final Number n){
        return n instanceof BigInteger ? (BigInteger)n : BigInteger.valueOf(n.longValue());
    }

    /**
     * Converts {@link Number} to {@link BigDecimal}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigDecimal numberToDecimal(final Number n){
        return n instanceof BigDecimal ? (BigDecimal)n : BigDecimal.valueOf(n.doubleValue());
    }

    /**
     * Converts {@link Number} to {@link java.util.Date}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Date numberToDate(final Number n){
        return new Date(n.longValue());
    }

    /**
     * Converts {@link Number} to {@link java.util.Calendar}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Calendar numberToCalendar(final Number n){
        final Calendar now = Calendar.getInstance();
        now.setTime(new Date(n.longValue()));
        return now;
    }

    /**
     * Converts {@link Number} to {@link Boolean}.
     * @param n The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Boolean numberToBoolean(final Number n){
        if(n instanceof BigInteger) return !BigInteger.ZERO.equals(n);
        else if(n instanceof BigDecimal) return !BigDecimal.ZERO.equals(n);
        else return n.longValue() != 0;
    }

    /**
     * Converts {@link Calendar} to {@link Long}.
     * @param c The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Long calendarToLong(final Calendar c){
        return c.getTime().getTime();
    }

    /**
     * Converts {@link Calendar} to {@link Date}.
     * @param c The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Date calendarToDate(final Calendar c){
        return c.getTime();
    }

    /**
     * Converts {@link Date} to {@link Calendar}.
     * @param d The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Calendar dateToCalendar(final Date d){
        final Calendar now = Calendar.getInstance();
        now.setTime(d);
        return now;
    }

    /**
     * Converts {@link Boolean} to {@link Byte}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Byte booleanToInt8(final Boolean b){
        return b ? (byte)1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Short}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Short booleanToInt16(final Boolean b){
        return b ? (short)1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Integer}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Integer booleanToInt32(final Boolean b){
        return b ? 1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Long}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Long booleanToInt64(final Boolean b){
        return b ? 1L : 0L;
    }

    /**
     * Converts {@link Boolean} to {@link BigInteger}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigInteger booleanToInteger(final Boolean b){
        return b ? BigInteger.ONE : BigInteger.ZERO;
    }

    /**
     * Converts {@link Boolean} to {@link BigDecimal}.
     * @param b The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static BigDecimal booleanToDecimal(final Boolean b){
        return b ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /**
     * Converts {@link String} to {@link Character}.
     * @param str The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Character stringToCharacter(final String str){
        return str.length() > 0 ? str.charAt(0) : '\0';
    }

    /**
     * Converts {@link Date} to {@link Long}.
     * @param d The value to convert.
     * @return The conversion result.
     */
    @Converter
    public static Long dateToLong(final Date d){
        return d.getTime();
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Byte}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Byte}.
     */
    public final static boolean supportsInt8(final ManagedEntityType entityType){
        return supportsProjection(entityType, Byte.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Byte}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt8Type(){
        return createEntitySimpleType(Byte.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Short}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Short}.
     */
    public final static boolean supportsInt16(final ManagedEntityType entityType){
        return supportsProjection(entityType, Short.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Short}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt16Type(){
        return createEntitySimpleType(Short.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Integer}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Integer}.
     */
    public final static boolean supportsInt32(final ManagedEntityType entityType){
        return supportsProjection(entityType, Integer.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Integer}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt32Type(){
        return createEntitySimpleType(Integer.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Long}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Long}.
     */
    public final static boolean supportsInt64(final ManagedEntityType entityType){
        return supportsProjection(entityType, Long.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Long}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt64Type(){
        return createEntitySimpleType(Long.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigInteger}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigInteger}.
     */
    public final static boolean supportsInteger(final ManagedEntityType entityType){
        return supportsProjection(entityType, BigInteger.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigInteger}.
     * @return A new type converter.
     */
    public final ManagedEntityType createIntegerType(){
        return createEntitySimpleType(BigInteger.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigDecimal}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigDecimal}.
     */
    public final static boolean supportsDecimal(final ManagedEntityType entityType){
        return supportsProjection(entityType, BigDecimal.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigDecimal}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDecimalType(){
        return createEntitySimpleType(BigDecimal.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Date}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Date}.
     */
    public final static boolean supportsUnixTime(final ManagedEntityType entityType){
        return supportsProjection(entityType, Date.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Date}.
     * @return A new type converter.
     */
    public final ManagedEntityType createUnixTimeType(){
        return createEntitySimpleType(Date.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Boolean}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Boolean}.
     */
    public final static boolean supportsBoolean(final ManagedEntityType entityType){
        return supportsProjection(entityType, Boolean.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Boolean}.
     * @return A new type converter.
     */
    public final ManagedEntityType createBooleanType(){
        return createEntitySimpleType(Boolean.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link String}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link String}.
     */
    public final static boolean supportsString(final ManagedEntityType entityType){
        return supportsProjection(entityType, String.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link String}.
     * @return A new type converter.
     */
    public final ManagedEntityType createStringType(){
        return createEntitySimpleType(String.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Float}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Float}.
     */
    public final static boolean supportsFloat(final ManagedEntityType entityType){
        return supportsProjection(entityType, Float.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Float}.
     * @return A new type converter.
     */
    public final ManagedEntityType createFloatType(){
        return createEntitySimpleType(Float.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Double}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Double}.
     */
    public final static boolean supportsDouble(final ManagedEntityType entityType){
        return supportsProjection(entityType, Double.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Double}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDoubleType(){
        return createEntitySimpleType(Double.class);
    }

    /**
     * Creates a new array type that can be converted into {@link Object[]}.
     * @param elementType An element type of the array.
     * @return A new array type that can be converted into {@link Object[]}.
     */
    public final AbstractManagedEntityType createEntityArrayType(final ManagedEntityType elementType){
        return createEntityType(new Factory<ManagedEntityArrayType>(){

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public ManagedEntityArrayType create() {
                return new ManagedEntityArrayType(elementType);
            }
        }, Object[].class);
    }

    /**
     * Creates a new tabular type that can be converted into {@link Table}.
     * <p>
     *  The returned {@link ManagedEntityTabularType} instance throws {@link UnsupportedOperationException}
     *  exception when {@link ManagedEntityTabularType#getRowCount()} is invoked.
     * </p>
     * @param columns A collection of columns.
     * @param index An array of indexed columns.
     * @return A new instance of the tabular type.
     */
    public final ManagedEntityType createEntityTabularType(final Map<String, ManagedEntityType> columns, final String... index){
        final Map<String, ManagedEntityType> readonlyColumns = Collections.unmodifiableMap(columns);
        final Collection<String> readonlyIndex = Collections.unmodifiableCollection(Arrays.asList(index));
        return createEntityType(new Factory<AbstractManagedEntityTabularType>(){

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public AbstractManagedEntityTabularType create() {
                return new AbstractManagedEntityTabularType() {
                    @Override
                    public final Collection<String> getColumns() {
                        return readonlyColumns.keySet();
                    }

                    /**
                     * Determines whether the specified column is indexed.
                     *
                     * @param column The name of the column.
                     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
                     */
                    @Override
                    public final boolean isIndexed(final String column) {
                        return readonlyIndex.contains(column);
                    }

                    @Override
                    public final ManagedEntityType getColumnType(final String column) {
                        return readonlyColumns.get(column);
                    }

                    @Override
                    public final long getRowCount() throws UnsupportedOperationException {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }, Table.class);
    }

    /**
     * Creates a new tabular type that can be converted into {@link Table}.
     * <p>
     *  The returned {@link ManagedEntityTabularType} returns {@code rowCount} from
     *  {@link ManagedEntityTabularType#getRowCount()} method.
     * </p>
     * @param columns A collection of columns.
     * @param rowCount A row count in the table.
     * @param index An array of indexed columns.
     * @return A new instance of the tabular type.
     */
    public final ManagedEntityType createEntityTabularType(final Map<String, ManagedEntityType> columns, final int rowCount, final String... index){
        final Map<String, ManagedEntityType> readonlyColumns = Collections.unmodifiableMap(columns);
        final Collection<String> readonlyIndex = Collections.unmodifiableCollection(Arrays.asList(index));
        return createEntityType(new Factory<AbstractManagedEntityTabularType>(){

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public AbstractManagedEntityTabularType create() {
                return new AbstractManagedEntityTabularType() {
                    @Override
                    public final Collection<String> getColumns() {
                        return readonlyColumns.keySet();
                    }

                    /**
                     * Determines whether the specified column is indexed.
                     *
                     * @param column The name of the column.
                     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
                     */
                    @Override
                    public final boolean isIndexed(final String column) {
                        return readonlyIndex.contains(column);
                    }

                    @Override
                    public final ManagedEntityType getColumnType(final String column) {
                        return readonlyColumns.get(column);
                    }

                    @Override
                    public final long getRowCount() {
                        return rowCount;
                    }
                };
            }
        }, Table.class);
    }
}
