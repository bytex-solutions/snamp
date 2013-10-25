package com.snamp.connectors;

import java.math.*;
import java.util.*;

/**
 * Represents builder for primitive types.
 * @author roman
 */
public class AttributePrimitiveTypeBuilder extends AttributeTypeInfoBuilder {

    /**
     * Initializes a new builder for primitive types.
     */
    public AttributePrimitiveTypeBuilder(){

    }

    /**
     * Converts any object to {@link String}.
     * @param obj
     * @return
     */
    @Converter
    public static String toString(final Object obj){
        return Objects.toString(obj, "");
    }

    /**
     * Converts {@link String} to {@link Byte}.
     * @param str
     * @return
     */
    @Converter
    public static Byte stringToInt8(final String str){
        return Byte.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Short}.
     * @param str
     * @return
     */
    @Converter
    public static Short stringToInt16(final String str){
        return Short.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Integer}.
     * @param str A string to convert.
     * @return
     */
    @Converter
    public static Integer stringToInt32(final String str){
        return Integer.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Long}.
     * @param str
     * @return
     */
    @Converter
    public static Long stringToInt64(final String str){
        return Long.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link BigInteger}.
     * @param str
     * @return
     */
    @Converter
    public static BigInteger stringToInteger(final String str){
        return new BigInteger(str);
    }

    /**
     * Converts {@link String} to {@link Boolean}.
     * @param str
     * @return
     */
    @Converter
    public static Boolean stringToBoolean(final String str){
        return Boolean.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Float}.
     * @param str
     * @return
     */
    @Converter
    public static Float stringToFloat(final String str){
        return Float.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link Double}.
     * @param str
     * @return
     */
    @Converter
    public static Double stringToDouble(final String str){
        return Double.valueOf(str);
    }

    /**
     * Converts {@link String} to {@link BigDecimal}.
     * @param str
     * @return
     */
    @Converter
    public static BigDecimal stringToDecimal(final String str){
        return new BigDecimal(str);
    }

    /**
     * Converts {@link Number} to {@link Byte}.
     * @param n
     * @return
     */
    @Converter
    public static Byte numberToInt8(final Number n){
        return n.byteValue();
    }

    /**
     * Converts {@link Number} to {@link Short}.
     * @param n
     * @return
     */
    @Converter
    public static Short numberToInt16(final Number n){
        return n.shortValue();
    }

    /**
     * Converts {@link Number} to {@link Integer}.
     * @param n
     * @return
     */
    @Converter
    public static Integer numberToInt32(final Number n){
        return n.intValue();
    }

    /**
     * Converts {@link Number} to {@link Long}.
     * @param n
     * @return
     */
    @Converter
    public static Long numberToInt64(final Number n){
        return n.longValue();
    }

    /**
     * Converts {@link Number} to {@link Float}.
     * @param n
     * @return
     */
    @Converter
    public static Float numberToFloat(final Number n){
        return n.floatValue();
    }

    /**
     * Converts {@link Number} to {@link Double}.
     * @param n
     * @return
     */
    @Converter
    public static Double numberToDouble(final Number n){
        return n.doubleValue();
    }

    /**
     * Converts {@link Number} to {@link BigInteger}.
     * @param n
     * @return
     */
    @Converter
    public static BigInteger numberToInteger(final Number n){
        return n instanceof BigInteger ? (BigInteger)n : BigInteger.valueOf(n.longValue());
    }

    /**
     * Converts {@link Number} to {@link BigDecimal}.
     * @param n
     * @return
     */
    @Converter
    public static BigDecimal numberToDecimal(final Number n){
        return n instanceof BigDecimal ? (BigDecimal)n : BigDecimal.valueOf(n.doubleValue());
    }

    /**
     * Converts {@link Number} to {@link Date}.
     * @param n
     * @return
     */
    @Converter
    public static Date numberToDate(final Number n){
        return new Date(n.longValue());
    }

    /**
     * Converts {@link Number} to {@link Calendar}.
     * @param n
     * @return
     */
    @Converter
    public static Calendar numberToCalendar(final Number n){
        final Calendar now = Calendar.getInstance();
        now.setTime(new Date(n.longValue()));
        return now;
    }

    /**
     * Converts {@link Number} to {@link Boolean}.
     * @param n
     * @return
     */
    @Converter
    public static Boolean numberToBoolean(final Number n){
        if(n instanceof BigInteger) return !BigInteger.ZERO.equals(n);
        else if(n instanceof BigDecimal) return !BigDecimal.ZERO.equals(n);
        else return n.longValue() != 0;
    }

    /**
     * Converts {@link Calendar} to {@link Long}.
     * @param c
     * @return
     */
    @Converter
    public static Long calendarToLong(final Calendar c){
        return c.getTime().getTime();
    }

    /**
     *
     * @param c
     * @return
     */
    @Converter
    public static Date calendarToDate(final Calendar c){
        return c.getTime();
    }

    /**
     * Converts {@link Date} to {@link Calendar}.
     * @param d
     * @return
     */
    @Converter
    public static Calendar dateToCalendar(final Date d){
        final Calendar now = Calendar.getInstance();
        now.setTime(d);
        return now;
    }

    /**
     * Converts {@link Boolean} to {@link Byte}.
     * @param b
     * @return
     */
    @Converter
    public static Byte booleanToInt8(final Boolean b){
        return b ? (byte)1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Short}.
     * @param b
     * @return
     */
    @Converter
    public static Short booleanToInt16(final Boolean b){
        return b ? (short)1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Integer}.
     * @param b
     * @return
     */
    @Converter
    public static Integer booleanToInt32(final Boolean b){
        return b ? 1 : 0;
    }

    /**
     * Converts {@link Boolean} to {@link Long}.
     * @param b
     * @return
     */
    @Converter
    public static Long booleanToInt64(final Boolean b){
        return b ? 1L : 0L;
    }

    /**
     * Converts {@link Boolean} to {@link BigInteger}.
     * @param b
     * @return
     */
    @Converter
    public static BigInteger booleanToInteger(final Boolean b){
        return b ? BigInteger.ONE : BigInteger.ZERO;
    }

    /**
     * Converts {@link Boolean} to {@link BigDecimal}.
     * @param b
     * @return
     */
    @Converter
    public static BigDecimal booleanToDecimal(final Boolean b){
        return b ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /**
     * Converts {@link String} to {@link Character}.
     * @param str
     * @return
     */
    @Converter
    public static Character stringToCharacter(final String str){
        return str.length() > 0 ? str.charAt(0) : '\0';
    }

    /**
     * Converts {@link Date} to {@link Long}.
     * @param d
     * @return
     */
    @Converter
    public static Long dateToLong(final Date d){
        return d.getTime();
    }



    public static AttributeConvertibleTypeInfo<Byte> createInt8Type(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Byte.class);
    }

    public final AttributeConvertibleTypeInfo<Byte> createInt8Type(){
        return createInt8Type(getClass());
    }

    public static boolean isInt8(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Byte.class);
    }

    public static AttributeConvertibleTypeInfo<Short> createInt16Type(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Short.class);
    }

    public final AttributeConvertibleTypeInfo<Short> createInt16Type(){
        return createInt16Type(getClass());
    }

    public static boolean isInt16(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Short.class);
    }

    public final AttributeConvertibleTypeInfo<Integer> createInt32Type(){
        return createInt32Type(getClass());
    }

    public static AttributeConvertibleTypeInfo<Integer> createInt32Type(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Integer.class);
    }

    public static boolean isInt32(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Integer.class);
    }

    public final AttributeConvertibleTypeInfo<Long> createInt64Type(){
        return createInt64Type(getClass());
    }

    public static AttributeConvertibleTypeInfo<Long> createInt64Type(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Long.class);
    }

    public static boolean isInt64(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Long.class);
    }

    public static AttributeConvertibleTypeInfo<BigInteger> createIntegerType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, BigInteger.class);
    }

    public final AttributeConvertibleTypeInfo<BigInteger> createIntegerType(){
        return createIntegerType(getClass());
    }

    public static boolean isInteger(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, BigInteger.class);
    }

    public static AttributeConvertibleTypeInfo<BigDecimal> createDecimalType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, BigDecimal.class);
    }

    public AttributeConvertibleTypeInfo<BigDecimal> createDecimalType(){
        return createTypeInfo(BigDecimal.class);
    }

    public static boolean isDecimal(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, BigDecimal.class);
    }

    public static AttributeConvertibleTypeInfo<String> createStringType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, String.class);
    }

    public final AttributeConvertibleTypeInfo<String> createStringType(){
        return createStringType(getClass());
    }

    public static boolean isString(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, String.class);
    }

    public static AttributeConvertibleTypeInfo<Date> createUnixTimeType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Date.class);
    }

    public final AttributeConvertibleTypeInfo<Date> createUnixTimeType(){
        return createUnixTimeType(getClass());
    }

    public static boolean isUnixTime(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Date.class);
    }

    public static AttributeConvertibleTypeInfo<Boolean> createBooleanType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Boolean.class);
    }

    public final AttributeConvertibleTypeInfo<Boolean> createBooleanType(){
        return createBooleanType(getClass());
    }

    public static boolean isBoolean(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Boolean.class);
    }

    public static AttributeConvertibleTypeInfo<Float> createFloatType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Float.class);
    }

    public final AttributeConvertibleTypeInfo<Float> createFloatType(){
        return createFloatType(getClass());
    }

    public static boolean isFloat(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Float.class);
    }

    public static AttributeConvertibleTypeInfo<Double> createDoubleType(final Class<? extends AttributeTypeInfoBuilder> builderType){
        return createTypeInfo(builderType, Double.class);
    }

    public final AttributeConvertibleTypeInfo<Double> createDoubleType(){
        return createDoubleType(getClass());
    }

    public static boolean isDouble(final com.snamp.connectors.AttributeTypeInfo attributeType){
        return isTypeOf(attributeType, Double.class);
    }
}
