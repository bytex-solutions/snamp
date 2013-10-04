package com.snamp.connectors;

import com.snamp.FilteredArrayIterator;

import java.math.*;
import java.util.*;

/**
 * Represents a set of primitive types.
 * @author roman
 */
public enum AttributePrimitiveType implements AttributeTypeInfo {
    /**
     * Represents double-precision floating point.
     */
    DOUBLE(double.class),

    /**
     * Represents single-precision floating point.
     */
    FLOAT(float.class),

    /**
     * Represents string type.
     */
    TEXT(String.class),
    /**
     * Represents boolean type.
     */
    BOOL(boolean.class),
    /**
     * Represents signed 8-bit integer type.
     */
    INT8(byte.class),

    /**
     * Represents signed 16-bit integer type.
     */
    INT16(short.class),

    /**
     * Represents signed 32-bit integer type.
     */
    INT32(int.class),

    /**
     * Represents signed 64-bit integer type.
     */
    INT64(long.class),

    /**
     * Represents signed arbitrary-precision decimal type.
     */
    DECIMAL(BigDecimal.class),

    /**
     * Represents signed arbitrary-precision integer type.
     */
    INTEGER(BigInteger.class),

    /**
     * Represents unix time,
     */
    UNIX_TIME(Date.class);

    private final Class<?> nativeClass;

    private AttributePrimitiveType(final Class<?> nativeClass){
        this.nativeClass = nativeClass;
    }

    /**
     * Returns the wrapper class for the primitive type.
     * @param classInfo The class information about primitive type.
     * @return The wrapper class for the primitive type.
     */
    public static Class<?> normalizeClass(final Class<?> classInfo){
        switch (classInfo.getCanonicalName()){
            case "byte": return Byte.class;
            case "short": return Short.class;
            case "int": return Integer.class;
            case "long": return Long.class;
            case "boolean": return Boolean.class;
            case "void": return Void.class;
            case "float": return Float.class;
            case "double": return Double.class;
            case "char": return Character.class;
            default: return classInfo;
        }
    }

    /**
     * Represents value converters.
     * @param <TSource> The type of the source value.
     * @param <TDestination> The type of the conversion result.
     */
    private static interface Converter<TSource, TDestination>{
        /**
         * Provides conversion from the specified value.
         * @param source The source value to convert.
         * @return
         */
        public TDestination convert(final TSource source);
    }

    /**
     * Represents converters.
     */
    private static enum ConversionProvider{

        /**
         * Represents Object-to-String converter.
         */
        ObjectToString(Object.class, String.class, new Converter<Object, String>() {
            @Override
            public final String convert(final Object o) {
                return Objects.toString(o, "");
            }
        }),

        /**
         * Represents String-to-Byte converter.
         */
        StringToInt8(String.class, Byte.class, new Converter<String, Byte>() {
            @Override
            public final Byte convert(final String s) {
                return Byte.valueOf(s);
            }
        }),

        /**
         * Represents String-to-Short converter.
         */
        StringToInt16(String.class, Short.class, new Converter<String, Short>() {
            @Override
            public final Short convert(final String s) {
                return Short.valueOf(s);
            }
        }),

        /**
         * Represents String-to-Integer converter.
         */
        StringToInt32(String.class, Integer.class, new Converter<String, Integer>() {
            @Override
            public Integer convert(final String s) {
                return Integer.valueOf(s);
            }
        }),

        /**
         * Represents String-to-Long converter.
         */
        StringToInt64(String.class, Long.class, new Converter<String, Long>() {
            @Override
            public Long convert(final String s) {
                return Long.valueOf(s);
            }
        }),

        /**
         * Represents String-to-BigInteger converter.
         */
        StringToInteger(String.class, BigInteger.class, new Converter<String, BigInteger>() {
            @Override
            public BigInteger convert(final String s) {
                return new BigInteger(s);
            }
        }),

        /**
         * Represents String-to-BigDecimal converter.
         */
        StringToDecimal(String.class, BigDecimal.class, new Converter<String, BigDecimal>() {
            @Override
            public BigDecimal convert(final String s) {
                return new BigDecimal(s);
            }
        }),

        /**
         * Represents String-to-Boolean converter.
         */
        StringToBoolean(String.class, Boolean.class, new Converter<String, Boolean>() {
            @Override
            public Boolean convert(final String s) {
                return Boolean.valueOf(s);
            }
        }),

        /**
         * Represents Number-to-Byte converter.
         */
        NumberToInt8(Number.class, Byte.class, new Converter<Number, Byte>() {
            @Override
            public Byte convert(final Number n) {
                return n.byteValue();
            }
        }),

        /**
         * Represents Number-to-Short converter.
         */
        NumberToInt16(Number.class, Short.class, new Converter<Number, Short>() {
            @Override
            public Short convert(final Number n) {
                return n.shortValue();
            }
        }),

        /**
         * Represents Number-to-Integer converter.
         */
        NumberToInt32(Number.class, Integer.class, new Converter<Number, Integer>() {
            @Override
            public Integer convert(final Number n) {
                return n.intValue();
            }
        }),

        /**
         * Represents Number-to-Long converter.
         */
        NumberToInt64(Number.class, Long.class, new Converter<Number, Long>() {
            @Override
            public Long convert(final Number n) {
                return n.longValue();
            }
        }),

        /**
         * Represents Number-to-BigInteger converter.
         */
        NumberToInteger(Number.class, BigInteger.class, new Converter<Number, BigInteger>() {
            @Override
            public BigInteger convert(final Number n) {
                return BigInteger.valueOf(n.longValue());
            }
        }),

        /**
         * Represents Number-to-BigDecimal converter.
         */
        NumberToDecimal(Number.class, BigDecimal.class, new Converter<Number, BigDecimal>() {
            @Override
            public BigDecimal convert(final Number n) {
                return BigDecimal.valueOf(n.longValue());
            }
        }),

        /**
         * Represents Number-to-Date converter.
         */
        NumberToDate(Number.class, Date.class, new Converter<Number, Date>() {
            @Override
            public Date convert(final Number n) {
                return new Date(n.longValue());
            }
        }),

        /**
         * Represents Number-to-Calendar converter.
         */
        NumberToCalendar(Number.class, Calendar.class, new Converter<Number, Calendar>() {
            @Override
            public Calendar convert(final Number n) {
                final Calendar now = Calendar.getInstance();
                now.setTime(new Date(n.longValue()));
                return now;
            }
        }),

        /**
         * Represents Number-to-Float converter.
         */
        NumberToFloat(Number.class, Float.class, new Converter<Number, Float>() {
            @Override
            public Float convert(final Number n) {
                return n.floatValue();
            }
        }),

        /**
         * Represents Number-to-Double converter.
         */
        NumberToDouble(Number.class, Double.class, new Converter<Number, Double>() {
            @Override
            public Double convert(final Number n) {
                return n.doubleValue();
            }
        }),

        /**
         * Represents Byte-to-Boolean converter.
         */
        Int8ToBoolean(Byte.class, Boolean.class, new Converter<Byte, Boolean>() {
            @Override
            public Boolean convert(final Byte b) {
                return b != 0;
            }
        }),

        /**
         * Represents Short-to-Boolean converter.
         */
        Int16ToBoolean(Short.class, Boolean.class, new Converter<Short, Boolean>() {
            @Override
            public Boolean convert(final Short aShort) {
                return aShort != 0;
            }
        }),

        /**
         * Represents Integer-to-Boolean converter.
         */
        Int32ToBoolean(Integer.class, Boolean.class, new Converter<Integer, Boolean>() {
            @Override
            public Boolean convert(final Integer i) {
                return i != 0;
            }
        }),

        /**
         * Represents Long-to-Boolean converter,
         */
        Int64ToBoolean(Long.class, Boolean.class, new Converter<Long, Boolean>() {
            @Override
            public Boolean convert(final Long aLong) {
                return aLong != 0;
            }
        }),

        /**
         * Represents BigInteger-to-Boolean converter.
         */
        IntegerToBoolean(BigInteger.class, Boolean.class, new Converter<BigInteger, Boolean>() {
            @Override
            public Boolean convert(final BigInteger i) {
                return !BigInteger.ZERO.equals(i);
            }
        }),

        /**
         * Represents BigDecimal-to-Boolean converter.
         */
        DecimalToBoolean(BigDecimal.class, Boolean.class, new Converter<BigDecimal, Boolean>() {
            @Override
            public Boolean convert(final BigDecimal d) {
                return !BigDecimal.ZERO.equals(d);
            }
        }),

        /**
         * Represents Char-to-Boolean converter.
         */
        CharToBoolean(Character.class, Boolean.class, new Converter<Character, Boolean>() {
            @Override
            public Boolean convert(final Character ch) {
                return ch > 0;
            }
        }),

        /**
         * Represents String-to-Char converter.
         */
        StringToChar(String.class, Character.class, new Converter<String, Character>() {
            @Override
            public Character convert(final String s) {
                return s.length() > 0 ? s.charAt(0) : '\0';
            }
        }),

        /**
         * Represents Date-to-Byte class.
         */
        DateToInt8(Date.class, Byte.class, new Converter<Date, Byte>() {
            @Override
            public Byte convert(final Date d) {
                return (byte)d.getTime();
            }
        }),

        /**
         * Represents Date-to-Short class.
         */
        DateToInt16(Date.class, Short.class, new Converter<Date, Short>() {
            @Override
            public Short convert(final Date d) {
                return (short)d.getTime();
            }
        }),

        /**
         * Represents Date-to-Integer class.
         */
        DateToInt32(Date.class, Integer.class, new Converter<Date, Integer>() {
            @Override
            public Integer convert(final Date d) {
                return (int)d.getTime();
            }
        }),

        /**
         * Represents Date-to-Long converter.
         */
        DateToInt64(Date.class, Long.class, new Converter<Date, Long>() {
            @Override
            public Long convert(final Date d) {
                return d.getTime();
            }
        }),

        /**
         * Represents Date-to-BigInteger converter.
         */
        DateToInteger(Date.class, BigInteger.class, new Converter<Date, BigInteger>() {
            @Override
            public BigInteger convert(final Date d) {
                return BigInteger.valueOf(d.getTime());
            }
        }),

        /**
         * Represents Date-to-BigDecimal converter,
         */
        DateToDecimal(Date.class, BigDecimal.class, new Converter<Date, BigDecimal>() {
            @Override
            public BigDecimal convert(final Date d) {
                return BigDecimal.valueOf(d.getTime());
            }
        }),

        /**
         * Represents Calendar-to-Byte class.
         */
        CalendarToInt8(Calendar.class, Byte.class, new Converter<Calendar, Byte>() {
            @Override
            public Byte convert(final Calendar d) {
                return (byte)d.getTime().getTime();
            }
        }),

        /**
         * Represents Calendar-to-Short class.
         */
        CalendarToInt16(Calendar.class, Short.class, new Converter<Calendar, Short>() {
            @Override
            public Short convert(final Calendar d) {
                return (short)d.getTime().getTime();
            }
        }),

        /**
         * Represents Calendar-to-Integer class.
         */
        CalendarToInt32(Calendar.class, Integer.class, new Converter<Calendar, Integer>() {
            @Override
            public Integer convert(final Calendar d) {
                return (int)d.getTime().getTime();
            }
        }),

        /**
         * Represents Calendar-to-Long converter.
         */
        CalendarToInt64(Calendar.class, Long.class, new Converter<Calendar, Long>() {
            @Override
            public Long convert(final Calendar d) {
                return d.getTime().getTime();
            }
        }),

        /**
         * Represents Calendar-to-BigInteger converter.
         */
        CalendarToInteger(Calendar.class, BigInteger.class, new Converter<Calendar, BigInteger>() {
            @Override
            public BigInteger convert(final Calendar d) {
                return BigInteger.valueOf(d.getTime().getTime());
            }
        }),

        /**
         * Represents Calendar-to-BigDecimal converter,
         */
        CalendarToDecimal(Calendar.class, BigDecimal.class, new Converter<Calendar, BigDecimal>() {
            @Override
            public BigDecimal convert(final Calendar d) {
                return BigDecimal.valueOf(d.getTime().getTime());
            }
        })

        ;
        private final Converter<?, ?> converter;
        private final Class<?> sourceClass;
        private final Class<?> destinationClass;

        private <TSource, TDestination> ConversionProvider(final Class<TSource> sourceClass, final Class<TDestination> destinationClass, final Converter<TSource, TDestination> converter){
            this.converter = converter;
            this.sourceClass = sourceClass;
            this.destinationClass = destinationClass;
        }

        /**
         * Determines whether the source type is supported.
         * @param sourceClass
         * @return
         */
        public final boolean canConvertFrom(final Class<?> sourceClass){
            return sourceClass.isPrimitive() ? canConvertFrom(normalizeClass(sourceClass)) :this.sourceClass.isAssignableFrom(sourceClass);
        }

        /**
         * Determines whether the destination type is supported.
         * @param destinationClass
         * @return
         */
        public final boolean canConvertTo(final Class<?> destinationClass){
            return destinationClass.isPrimitive() ? canConvertTo(normalizeClass(destinationClass)) : destinationClass.isAssignableFrom(this.destinationClass);
        }

        /**
         * Returns an iterator through converters with supported conversion result.
         * @param destinationClass
         * @return
         */
        public static final Iterable<ConversionProvider> convertersTo(final Class<?> destinationClass){
            return new Iterable<ConversionProvider>() {
                @Override
                public Iterator<ConversionProvider> iterator() {
                    return new FilteredArrayIterator<ConversionProvider>(values()) {
                        @Override
                        protected boolean filter(final ConversionProvider element) {
                            return element.canConvertTo(destinationClass);
                        }
                    };
                }
            };
        }

        /**
         * Returns an iterator through converters with supported conversion source.
         * @param sourceClass
         * @return
         */
        public static final Iterable<ConversionProvider> convertersFrom(final Class<?> sourceClass){
            return new Iterable<ConversionProvider>() {
                @Override
                public Iterator<ConversionProvider> iterator() {
                    return new FilteredArrayIterator<ConversionProvider>(values()) {
                        @Override
                        protected boolean filter(final ConversionProvider element) {
                            return false;  //To change body of implemented methods use File | Settings | File Templates.
                        }
                    };
                }
            };
        }

        /**
         *
         * @param source
         * @param <TSource>
         * @param <TDestination>
         * @return
         */
        public final <TSource, TDestination> TDestination convert(final TSource source){
            return ((Converter<TSource, TDestination>)converter).convert(source);
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
        if(target == null) return false;
        else if(target.isAssignableFrom(nativeClass)) return true;
        else for(final ConversionProvider converter: ConversionProvider.convertersTo(target))
                if(converter.canConvertFrom(nativeClass)) return true;
        return false;
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
    public final <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
        if(target == null) throw new IllegalArgumentException("target is null.");
        else if(value == null) return null;
        else if(target.isAssignableFrom(value.getClass())) return (T)value;
        else for(final ConversionProvider converter: ConversionProvider.convertersTo(target))
                if(converter.canConvertFrom(nativeClass)) return converter.convert(value);
        throw new IllegalArgumentException(String.format("Class %s is not supported.", target));
    }

    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     *
     * @param source The type of the value that can be converted to the attribute value.
     * @param <T>    The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    @Override
    public final <T> boolean canConvertFrom(final Class<T> source) {
        if(source == null) return false;
        else if(source.isAssignableFrom(nativeClass)) return true;
        else for(final ConversionProvider converter: ConversionProvider.convertersFrom(source))
                if(converter.canConvertTo(nativeClass)) return true;
        return false;
    }

    /**
     * Returns a primitive type converted from the specified value.
     * @param value
     * @return
     */
    public final Object convertFrom(final Object value){
        if(nativeClass.isInstance(value)) return value;
        else for(final ConversionProvider converter: ConversionProvider.convertersTo(nativeClass))
                if(converter.canConvertFrom(value.getClass())) return converter.convert(value);
        throw new IllegalArgumentException(String.format("Value %s is not supported.", value));
    }
}
