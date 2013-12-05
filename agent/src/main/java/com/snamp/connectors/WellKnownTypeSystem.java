package com.snamp.connectors;

import com.snamp.Table;

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
 *         <li>Tabular and dictionary data should be convertible to {@link com.snamp.Table} type. An implementation
 *         for this interface is provided by {@link com.snamp.SimpleTable} class. Entity data type
 *         should implements {@link EntityTabularType} interface.</li>
 *         <li>Array data should be convertible to Java array and {@link com.snamp.Table} type. Attribute
 *         data type should inherits from {@link AttributeArrayType} class.</li>
 *     </ul>
 * </p>
 * <p>
 * Management entity type system is a set of converters that provides conversion between MIB-specific
 * data types and universal data types. This class provides set of converters between these data types
 * in the form of static public unary methods annotated with {@link EntityTypeInfoBuilder.Converter} interface. Typically,
 * each custom SNAMP connector contains its own type system converter, inherited from this class.
 * The following example demonstrates your own type system converter:
 * <pre><code>
 * public final class CustomTypeInfoBuilder extends WellKnownTypeSystem&lt;AttributeTypeInfo&gt;{
 *     public CustomTypeInfoBuilder(){
 *       super(AttributeTypeInfo.class);
 *     }
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
 *     public final AttributeTypeConverter createByteArrayType(){
 *       return createTypeInfo(getClass(), byte[].class, String.class);
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
public class WellKnownTypeSystem<E extends EntityTypeInfo> extends EntityTypeInfoBuilder<E> {

    /**
     * Initializes a new type system for the specified management entity.
     * @param entityType Management entity type. Cannot be {@literal null}.
     * @throws IllegalArgumentException entityType is {@literal null}.
     */
    public WellKnownTypeSystem(final Class<E> entityType){
        super(entityType);
    }

    /**
     * Converts any object to {@link String}.
     * @param obj An object to convert.
     * @return A {@link String} representation of the input object.
     */
    @Converter
    public static String toString(final Object obj){
        return Objects.toString(obj, "");
    }

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
    public final static boolean isInt8(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Byte.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Byte}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createInt8Type(final Class<?> sourceType){
        return createTypeInfo(sourceType, Byte.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Byte}.
     * @return A new type converter.
     */
    public final E createInt8Type(){
        return createInt8Type(Byte.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Short}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Short}.
     */
    public final static boolean isInt16(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Short.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Short}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createInt16Type(final Class<?> sourceType){
        return createTypeInfo(sourceType, Short.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Short}.
     * @return A new type converter.
     */
    public final E createInt16Type(){
        return createInt16Type(Short.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Integer}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Integer}.
     */
    public final static boolean isInt32(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Integer.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Integer}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createInt32Type(final Class<?> sourceType){
        return createTypeInfo(sourceType, Integer.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Integer}.
     * @return A new type converter.
     */
    public final E createInt32Type(){
        return createInt32Type(Integer.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Long}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Long}.
     */
    public final static boolean isInt64(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Long.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Long}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createInt64Type(final Class<?> sourceType){
        return createTypeInfo(sourceType, Long.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Long}.
     * @return A new type converter.
     */
    public final E createInt64Type(){
        return createInt64Type(Long.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigInteger}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigInteger}.
     */
    public final static boolean isInteger(final EntityTypeInfo entityType){
        return isTypeOf(entityType, BigInteger.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link BigInteger}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createIntegerType(final Class<?> sourceType){
        return createTypeInfo(sourceType, BigInteger.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigInteger}.
     * @return A new type converter.
     */
    public final E createIntegerType(){
        return createIntegerType(BigInteger.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigDecimal}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigDecimal}.
     */
    public final static boolean isDecimal(final EntityTypeInfo entityType){
        return isTypeOf(entityType, BigDecimal.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link BigDecimal}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createDecimalType(final Class<?> sourceType){
        return createTypeInfo(sourceType, BigDecimal.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigDecimal}.
     * @return A new type converter.
     */
    public final E createDecimalType(){
        return createDecimalType(BigDecimal.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Date}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Date}.
     */
    public final static boolean isUnixTime(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Date.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Date}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createUnixTimeType(final Class<?> sourceType){
        return createTypeInfo(sourceType, Date.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Date}.
     * @return A new type converter.
     */
    public final E createUnixTimeType(){
        return createUnixTimeType(Date.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Boolean}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Boolean}.
     */
    public final static boolean isBoolean(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Boolean.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Boolean}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createBooleanType(final Class<?> sourceType){
        return createTypeInfo(sourceType, Boolean.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Boolean}.
     * @return A new type converter.
     */
    public final E createBooleanType(){
        return createBooleanType(Boolean.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link String}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link String}.
     */
    public final static boolean isString(final EntityTypeInfo entityType){
        return isTypeOf(entityType, String.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link String}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createStringType(final Class<?> sourceType){
        return createTypeInfo(sourceType, String.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link String}.
     * @return A new type converter.
     */
    public final E createStringType(){
        return createStringType(String.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Float}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Float}.
     */
    public final static boolean isFloat(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Float.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Float}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createFloatType(final Class<?> sourceType){
        return createTypeInfo(sourceType, Float.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Float}.
     * @return A new type converter.
     */
    public final E createFloatType(){
        return createFloatType(Float.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Double}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Double}.
     */
    public final static boolean isDouble(final EntityTypeInfo entityType){
        return isTypeOf(entityType, Double.class);
    }

    /**
     * Constructs a new type converter that provides conversion between MIB-specific
     * type and {@link Double}.
     * @param sourceType MIB-specific type of the management entity value.
     * @return A newly constructed type converter.
     */
    public final E createDoubleType(final Class<?> sourceType){
        return createTypeInfo(sourceType, Double.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Double}.
     * @return A new type converter.
     */
    public final E createDoubleType(){
        return createDoubleType(Double.class);
    }

    /**
     * Determines whether the specified management entity is a table.
     * @param entityType Type of the management entity.
     * @return {@literal true}, if the specified management entity is convertible to {@link Table} class;
     * otherwise, {@literal false}.
     */
    public static boolean isTable(final EntityTypeInfo entityType){
        return (entityType != null) && (entityType instanceof EntityTabularType || entityType.canConvertTo(Table.class));
    }

    /**
     * Determines whether the specified management entity is an array.
     * @param entityType Type of the management entity.
     * @return {@literal true}, if the specified management entity is convertible to {@link Object}[] class;
     * otherwise, {@literal false}.
     */
    public static boolean isArray(final EntityTypeInfo entityType){
        return entityType != null && entityType.canConvertTo(Object[].class);
    }
}
