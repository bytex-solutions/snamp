package com.bytex.snamp.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Convert;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.*;

/**
 * Describes a well-known type that should be supported by
 * resource connector and understood by gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum  WellKnownType implements Serializable, Type, Predicate<Object> {
    /**
     * Represents {@link java.lang.Void} data type.
     */
    VOID("void", SimpleType.VOID) {
        @Override
        public Void convert(final Object value) throws ClassCastException {
            throw new ClassCastException(String.format("Unable to convert value '%s' to void", value));
        }
    },

    /**
     * Represents {@link javax.management.ObjectName} data type.
     */
    OBJECT_NAME("objectname", SimpleType.OBJECTNAME) {
        @Override
        public ObjectName convert(final Object value) throws ClassCastException {
            return Convert.toType(value, ObjectName.class).orElseGet(() -> WellKnownType.parseObjectName(value.toString()));
        }
    },

    /**
     * Represents {@link java.lang.String} data type.
     */
    STRING("string", SimpleType.STRING) {
        @Override
        public String convert(final Object value) {
            return value.toString();
        }
    },

    /**
     * Represents {@link java.lang.Byte} data type.
     */
    BYTE("int8", SimpleType.BYTE) {
        @Override
        public Byte convert(final Object value) throws ClassCastException {
            return Convert.toByte(value, CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Character} data type.
     */
    CHAR("char", SimpleType.CHARACTER) {
        @Override
        public Character convert(final Object value) throws ClassCastException {
            return Convert.toChar(value, CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Short} data type.
     */
    SHORT("int16", SimpleType.SHORT) {
        @Override
        public Short convert(final Object value) throws ClassCastException {
            return Convert.toShort(value, CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Integer} data type.
     */
    INT("int32", SimpleType.INTEGER) {
        @Override
        public Integer convert(final Object value) throws ClassCastException {
            return Convert.toInt(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Long} data type.
     */
    LONG("int64", SimpleType.LONG) {
        @Override
        public Long convert(final Object value) throws ClassCastException {
            return Convert.toLong(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Boolean} data type.
     */
    BOOL("bool", SimpleType.BOOLEAN) {
        @Override
        public Boolean convert(final Object value) throws ClassCastException {
            return Convert.toBoolean(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Float} data type.
     */
    FLOAT("float32", SimpleType.FLOAT) {
        @Override
        public Float convert(final Object value) throws ClassCastException {
            return Convert.toFloat(value, CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Boolean} data type.
     */
    DOUBLE("float64", SimpleType.DOUBLE) {
        @Override
        public Double convert(final Object value) throws ClassCastException {
            return Convert.toDouble(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.util.Date} data type.
     */
    DATE("datetime", SimpleType.DATE) {
        @Override
        public Date convert(final Object value) throws ClassCastException {
            return Convert.toDate(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.math.BigInteger} data type.
     */
    BIG_INT("bigint", SimpleType.BIGINTEGER) {
        @Override
        public BigInteger convert(final Object value) throws ClassCastException {
            return Convert.toBigInteger(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.math.BigDecimal} data type.
     */
    BIG_DECIMAL("bigdecimal", SimpleType.BIGDECIMAL) {
        @Override
        public BigDecimal convert(final Object value) throws ClassCastException {
            return Convert.toBigDecimal(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.ByteBuffer} data type.
     */
    BYTE_BUFFER("buffer(int8)", ByteBuffer.class) {
        @Override
        public ByteBuffer convert(final Object value) throws ClassCastException {
            return Convert.toByteBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.CharBuffer} data type.
     */
    CHAR_BUFFER("buffer(char)", CharBuffer.class) {
        @Override
        public CharBuffer convert(final Object value) throws ClassCastException {
            return Convert.toCharBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.ShortBuffer} data type.
     */
    SHORT_BUFFER("buffer(int16)", ShortBuffer.class) {
        @Override
        public ShortBuffer convert(final Object value) throws ClassCastException {
            return Convert.toShortBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.IntBuffer} data type.
     */
    INT_BUFFER("buffer(int32)", IntBuffer.class) {
        @Override
        public IntBuffer convert(final Object value) throws ClassCastException {
            return Convert.toIntBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.LongBuffer} data type.
     */
    LONG_BUFFER("buffer(int64)", LongBuffer.class) {
        @Override
        public LongBuffer convert(final Object value) throws ClassCastException {
            return Convert.toLongBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.FloatBuffer} data type.
     */
    FLOAT_BUFFER("buffer(float32)", FloatBuffer.class) {
        @Override
        public FloatBuffer convert(final Object value) throws ClassCastException {
            return Convert.toFloatBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.nio.DoubleBuffer} data type.
     */
    DOUBLE_BUFFER("buffer(float64)", DoubleBuffer.class) {
        @Override
        public DoubleBuffer convert(final Object value) throws ClassCastException {
            return Convert.toDoubleBuffer(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@code byte[]} type.
     */
    BYTE_ARRAY("array(int8)", SimpleType.BYTE, true) {
        @Override
        public byte[] convert(final Object value) throws ClassCastException {
            return Convert.toByteArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Byte}[] type.
     */
    WRAPPED_BYTE_ARRAY("array(int8&)", SimpleType.BYTE, false) {
        @Override
        public Byte[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Byte[].class)
                    .orElseGet(() -> Convert.toByteArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code char[]} type.
     */
    CHAR_ARRAY("array(char)", SimpleType.CHARACTER, true) {
        @Override
        public char[] convert(final Object value) throws ClassCastException {
            return Convert.toCharArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Character}[] type.
     */
    WRAPPED_CHAR_ARRAY("array(char&)", SimpleType.CHARACTER, false) {
        @Override
        public Character[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Character[].class)
                    .orElseGet(() -> Convert.toCharArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code short[]} type.
     */
    SHORT_ARRAY("array(int16)", SimpleType.SHORT, true) {
        @Override
        public short[] convert(final Object value) throws ClassCastException {
            return Convert.toShortArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Short}[] type.
     */
    WRAPPED_SHORT_ARRAY("array(int16&)", SimpleType.SHORT, false) {
        @Override
        public Short[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Short[].class)
                    .orElseGet(() -> Convert.toShortArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code int[]} type.
     */
    INT_ARRAY("array(int32)", SimpleType.INTEGER, true) {
        @Override
        public int[] convert(final Object value) throws ClassCastException {
            return Convert.toIntArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Integer}[] type.
     */
    WRAPPED_INT_ARRAY("array(int32&)", SimpleType.INTEGER, false) {
        @Override
        public Integer[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Integer[].class)
                    .orElseGet(() -> Convert.toIntArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code long[]} type.
     */
    LONG_ARRAY("array(int64)", SimpleType.LONG, true) {
        @Override
        public long[] convert(final Object value) throws ClassCastException {
            return Convert.toLongArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Long}[] type.
     */
    WRAPPED_LONG_ARRAY("array(int64&)", SimpleType.LONG, false) {
        @Override
        public Long[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Long[].class)
                    .orElseGet(() -> Convert.toLongArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code boolean[]} type.
     */
    BOOL_ARRAY("array(bool)", SimpleType.BOOLEAN, true) {
        @Override
        public boolean[] convert(final Object value) throws ClassCastException {
            return Convert.toBooleanArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Boolean}[] type.
     */
    WRAPPED_BOOL_ARRAY("array(bool&)", SimpleType.BOOLEAN, false) {
        @Override
        public Boolean[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Boolean[].class)
                    .orElseGet(() -> Convert.toBooleanArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code float[]} type.
     */
    FLOAT_ARRAY("array(float32)", SimpleType.FLOAT, true) {
        @Override
        public float[] convert(final Object value) throws ClassCastException {
            return Convert.toFloatArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Float}[] type.
     */
    WRAPPED_FLOAT_ARRAY("array(float32&)", SimpleType.FLOAT, false) {
        @Override
        public Float[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Float[].class)
                    .orElseGet(() -> Convert.toFloatArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@code double[]} type.
     */
    DOUBLE_ARRAY("array(float64)", SimpleType.DOUBLE, true) {
        @Override
        public double[] convert(final Object value) throws ClassCastException {
            return Convert.toDoubleArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.lang.Double}[] type.
     */
    WRAPPED_DOUBLE_ARRAY("array(float64&)", SimpleType.DOUBLE, false) {
        @Override
        public Double[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, Double[].class)
                    .orElseGet(() -> Convert.toDoubleArray(value).map(ArrayUtils::wrapArray).orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@link java.lang.String}[] type.
     */
    STRING_ARRAY("array(string)", SimpleType.STRING, false) {
        @Override
        public String[] convert(final Object value) throws ClassCastException {
            return Convert.toStringArray(value).orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.util.Date}[] type.
     */
    DATE_ARRAY("array(datetime)", SimpleType.DATE, false) {
        @Override
        public Date[] convert(final Object value) throws ClassCastException {
            return Convert.toArray(value, Date[].class, Convert::toDate)
                    .orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.math.BigInteger}[] type.
     */
    BIG_INT_ARRAY("array(bigint)", SimpleType.BIGINTEGER, false) {
        @Override
        public BigInteger[] convert(final Object value) throws ClassCastException {
            return Convert.toArray(value, BigInteger[].class, Convert::toBigInteger)
                    .orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link java.math.BigDecimal}[] type.
     */
    BIG_DECIMAL_ARRAY("array(bigdecimal)", SimpleType.BIGDECIMAL, false) {
        @Override
        public BigDecimal[] convert(final Object value) throws ClassCastException {
            return Convert.toArray(value, BigDecimal[].class, Convert::toBigDecimal)
                    .orElseThrow(CONVERSION_EXCEPTION);
        }
    },

    /**
     * Represents {@link javax.management.ObjectName}[] type.
     */
    OBJECT_NAME_ARRAY("array(objectname)", SimpleType.OBJECTNAME, false) {
        @Override
        public ObjectName[] convert(final Object value) throws ClassCastException {
            return Convert.toType(value, ObjectName[].class)
                    .orElseGet(() -> Convert.toStringArray(value)
                            .map(strings -> ArrayUtils.transform(strings, ObjectName.class, WellKnownType::parseObjectName))
                            .orElseThrow(CONVERSION_EXCEPTION));
        }
    },

    /**
     * Represents {@link javax.management.openmbean.CompositeData} type.
     */
    DICTIONARY("dictionary", CompositeData.class) {
        @Override
        public CompositeData convert(final Object value) throws ClassCastException {
            return (CompositeData) value;
        }
    },

    /**
     * Represents {@link javax.management.openmbean.CompositeData}[] type.
     */
    DICTIONARY_ARRAY("array(dictionary)", CompositeData[].class) {
        @Override
        public CompositeData[] convert(final Object value) throws ClassCastException {
            return (CompositeData[]) value;
        }
    },

    /**
     * Represents {@link javax.management.openmbean.TabularData} type.
     */
    TABLE("table", TabularData.class) {
        @Override
        public TabularData convert(final Object value) throws ClassCastException {
            return (TabularData) value;
        }
    },

    /**
     * Represents {@link javax.management.openmbean.TabularData}[] type.
     */
    TABLE_ARRAY("array(table)", TabularData[].class) {
        @Override
        public TabularData[] convert(final Object value) throws ClassCastException {
            return (TabularData[]) value;
        }
    };

    private static final class WellKnownTypeCacheLoader extends CacheLoader<Object, WellKnownType>{
        private static InvalidKeyException cacheMissing(final Object key){
            return new InvalidKeyException("Well-known type is not defined for class " + key);
        }

        private static WellKnownType load(final String className) throws InvalidKeyException {
            for (final WellKnownType type : values())
                if (className.equals(type.getJavaType().getName()))
                    return type;
            throw cacheMissing(className);
        }

        private static WellKnownType load(final TypeToken<?> javaType) throws InvalidKeyException {
            if(javaType.isPrimitive())
                return load(javaType.wrap());
            else for(final WellKnownType type: values())
                if(javaType.isSupertypeOf(type.getJavaType()))
                    return type;
            throw cacheMissing(javaType);
        }

        private static WellKnownType load(final OpenType<?> openType) throws InvalidKeyException {
            if (openType instanceof CompositeType)
                return DICTIONARY;
            else if (openType instanceof TabularType)
                return TABLE;
            else for (final WellKnownType type : values())
                    if (Objects.equals(openType, type.getOpenType()))
                        return type;
            throw cacheMissing(openType);
        }

        @Override
        public WellKnownType load(@Nonnull final Object key) throws InvalidKeyException {
            if(key instanceof String)
                return load((String)key);
            else if(key instanceof TypeToken<?>)
                return load((TypeToken<?>)key);
            else if(key instanceof OpenType<?>)
                return load((OpenType<?>)key);
            else throw cacheMissing(key);
        }
    }

    private static final LoadingCache<Object, WellKnownType> CACHE =
            CacheBuilder.newBuilder().weakKeys().build(new WellKnownTypeCacheLoader());

    private static final Supplier<ClassCastException> CONVERSION_EXCEPTION = () -> new ClassCastException("Unsupported conversion");
    private final OpenType<?> openType;
    private final Class<?> javaType;
    private final String displayName;

    <T> WellKnownType(final String name, final SimpleType<T> openType){
        this.openType = Objects.requireNonNull(openType, "openType is null.");
        this.javaType = OpenTypes.getType(openType);
        this.displayName = name;
    }

    <A> WellKnownType(final String name, final SimpleType<?> componentType,
                      final boolean primitive){
        this.openType = callUnchecked(() -> new ArrayType<A>(componentType, primitive));
        this.javaType = OpenTypes.getType(openType);
        this.displayName = name;
    }

    WellKnownType(final String name, final Class<?> javaType) {
        this.openType = null;
        this.javaType = Objects.requireNonNull(javaType, "javaType is null.");
        this.displayName = name;
    }

    private static ObjectName parseObjectName(final String value) throws ClassCastException {
        return callAndWrapException(() -> new ObjectName(value), e -> wrapException(e.getMessage(), e, ClassCastException::new));
    }

    /**
     * Gets display name of this type.
     * @return The display name of this type.
     */
    public final String getDisplayName(){
        return displayName;
    }

    /**
     * Determines whether this type is compliant with JMX Open Type.
     * @return {@literal true}, if this type is compliant with JMX Open Type; otherwise, {@literal false}.
     * @see javax.management.openmbean.OpenType
     */
    public final boolean isOpenType(){
        return openType != null;
    }

    /**
     * Determines whether this type is a number type.
     * @return {@literal true}, if this type is a number type; otherwise, {@literal false}.
     */
    public final boolean isNumber(){
        return Number.class.isAssignableFrom(javaType);
    }

    /**
     * Determines whether this type describes an array.
     * <p>
     *     The following enum values recognized as arrays:
     *     <ul>
     *         <li>{@link #BYTE_ARRAY}</li>
     *         <li>{@link #WRAPPED_BYTE_ARRAY}</li>
     *         <li>{@link #CHAR_ARRAY}</li>
     *         <li>{@link #WRAPPED_CHAR_ARRAY}</li>
     *         <li>{@link #SHORT_ARRAY}</li>
     *         <li>{@link #WRAPPED_SHORT_ARRAY}</li>
     *         <li>{@link #INT_ARRAY}</li>
     *         <li>{@link #WRAPPED_INT_ARRAY}</li>
     *         <li>{@link #BOOL_ARRAY}</li>
     *         <li>{@link #WRAPPED_BOOL_ARRAY}</li>
     *         <li>{@link #LONG_ARRAY}</li>
     *         <li>{@link #WRAPPED_LONG_ARRAY}</li>
     *         <li>{@link #FLOAT_ARRAY}</li>
     *         <li>{@link #WRAPPED_FLOAT_ARRAY}</li>
     *         <li>{@link #DOUBLE_ARRAY}</li>
     *         <li>{@link #WRAPPED_DOUBLE_ARRAY}</li>
     *         <li>{@link #DATE_ARRAY}</li>
     *         <li>{@link #STRING_ARRAY}</li>
     *         <li>{@link #OBJECT_NAME_ARRAY}</li>
     *         <li>{@link #DICTIONARY_ARRAY}</li>
     *         <li>{@link #TABLE_ARRAY}</li>
     *     </ul>
     * @return {@literal true}, if this type describes an array; otherwise, {@literal false}.
     */
    public final boolean isArray(){
        return openType instanceof ArrayType<?> || javaType.isArray();
    }

    public final boolean isPrimitiveArray(){
        return Convert.toType(openType, ArrayType.class)
                .map(ArrayType::isPrimitiveArray)
                .orElse(Boolean.FALSE);
    }

    public final boolean isSimpleArray() {
        return Convert.toType(openType, ArrayType.class)
                .map(type -> type.getElementOpenType() instanceof SimpleType<?>)
                .orElse(Boolean.FALSE);
    }

    /**
     * Determines whether this type is scalar.
     * <p>
     *  The following enum values recognized as simple types:
     *     <ul>
     *         <li>{@link #BYTE}</li>
     *         <li>{@link #CHAR}</li>
     *         <li>{@link #SHORT}</li>
     *         <li>{@link #INT}</li>
     *         <li>{@link #LONG}</li>
     *         <li>{@link #BOOL}</li>
     *         <li>{@link #BIG_INT}</li>
     *         <li>{@link #BIG_DECIMAL}</li>
     *         <li>{@link #STRING}</li>
     *         <li>{@link #DATE}</li>
     *         <li>{@link #FLOAT}</li>
     *         <li>{@link #DOUBLE}</li>
     *     </ul>
     * @return {@literal true}, if this type is simple type; otherwise, {@literal false}.
     */
    public final boolean isSimple(){
        return openType instanceof SimpleType<?>;
    }

    /**
     * Determines whether this type a buffer.
     * <p>
     *  The following enum values recognized as buffer:
     *     <ul>
     *         <li>{@link #BYTE_BUFFER}</li>
     *         <li>{@link #SHORT_BUFFER}</li>
     *         <li>{@link #INT_BUFFER}</li>
     *         <li>{@link #CHAR_BUFFER}</li>
     *         <li>{@link #LONG_BUFFER}</li>
     *         <li>{@link #FLOAT_BUFFER}</li>
     *         <li>{@link #DOUBLE_BUFFER}</li>
     *     </ul>
     * @return {@literal true}, if this type is a buffer; otherwise, {@literal false}.
     */
    public final boolean isBuffer(){
        return Buffer.class.isAssignableFrom(javaType);
    }

    /**
     * Returns JMX open type associated with this SNAMP well-known type.
     * @return JMX open type associated with this SNAMP well-known type; or
     *      {@literal null} if there is no mapping to the SNAMP well-known type.
     */
    public final OpenType<?> getOpenType(){
        return openType;
    }

    /**
     * Determines whether the specified object is an instance of this well-known type.
     * @param value The value to check.
     * @return {@literal true}, if the specified object is an instance of this type;
     *      otherwise, {@literal false}.
     */
    @Override
    public final boolean test(final Object value) {
        return openType == null ? javaType.isInstance(value) : openType.isValue(value);
    }

    /**
     * Gets underlying Java type that represents this SNAMP well-known type.
     * @return The underlying Java type.
     */
    public final Class<?> getJavaType(){
        return javaType;
    }

    /**
     * Gets underlying Java type in form of the {@link com.google.common.reflect.TypeToken}.
     * @return The underlying Java type.
     */
    public final TypeToken<?> getTypeToken(){
        return TypeToken.of(getJavaType());
    }

    /**
     * Gets type by its display name.
     * @param displayName Display name of the type.
     * @return A type converted from its display name; or {@literal null}, if display name is invalid.
     */
    public static WellKnownType parse(final String displayName){
        for(final WellKnownType candidate: values())
            if(candidate.displayName.equals(displayName))
                return candidate;
        return null;
    }

    /**
     * Detects well-known SNAP type using Java class name.
     * @param className The name of the Java class.
     * @return Inferred well-known SNAMP type; or {@literal null}, if type cannot be inferred.
     * @see Class#getName()
     */
    public static WellKnownType getType(final String className) {
        if(className == null || className.isEmpty()) return null;
        else try {
            return CACHE.get(className);
        } catch (ExecutionException ignored) {
            return null;
        }
    }

    /**
     * Detects well-known SNAMP type using native Java type.
     * @param javaType Java type to be mapped into SNAMP well-known type.
     * @return Inferred well-known SNAMP type; or {@literal null}, if type cannot be inferred.
     */
    public static WellKnownType getType(final Class<?> javaType) {
        return javaType != null ? getType(TypeToken.of(javaType)) : null;
    }

    /**
     * Detects well-known SNAMP type using JMX Open Type.
     * @param openType JMX Open Type reference.
     * @return Inferred well-known SNAMP type; or {@literal null}, if type cannot be detected.
     */
    public static WellKnownType getType(final OpenType<?> openType) {
        if(openType == null) return null;
        //these conditions are necessary because instances of CompositeType and TabularType
        //can be created manually and it is possible to overflow cache and gets OutOfMemoryException
        else if (openType instanceof CompositeType)
            return DICTIONARY;
        else if (openType instanceof TabularType)
            return TABLE;
        else if(openType instanceof ArrayType<?>){
            final OpenType<?> elementType = ((ArrayType<?>)openType).getElementOpenType();
            if(elementType instanceof CompositeType)
                return DICTIONARY_ARRAY;
            else if(elementType instanceof TabularType)
                return TABLE_ARRAY;
        }
        try {
            return CACHE.get(openType);
        } catch (final ExecutionException ignored) {
            return null;
        }
    }

    /**
     * Detects well-known SNAMP type of the object.
     * @param value The object which type should be inferred.
     * @return Inferred well-known SNAMP type; or {@literal null} if type cannot be detected.
     */
    public static WellKnownType fromValue(final Object value) {
        if(value != null)
            for(final WellKnownType type: values())
                if(type.test(value))
                    return type;
        return null;
    }

    /**
     * Detects well-known SNAMP type using type token.
     * @param token Type token.
     * @return Inferred well-known SNAMP type; or {@literal null} if type cannot be detected.
     */
    public static WellKnownType getType(final TypeToken<?> token){
        if(token == null) return null;
        else try {
            return CACHE.get(token);
        } catch (final ExecutionException ignored) {
            return null;
        }
    }

    private static Set<WellKnownType> filterTypes(final Predicate<WellKnownType> filter) {
        return Collections.unmodifiableSet(Arrays.stream(values())
                .filter(filter)
                .collect(Collectors.toSet()));
    }

    public static Set<WellKnownType> getSimpleTypes(){
        return filterTypes(WellKnownType::isSimple);
    }

    public static Set<WellKnownType> getOpenTypes(){
        return filterTypes(WellKnownType::isOpenType);
    }

    public static Set<WellKnownType> getArrayTypes(){
        return filterTypes(WellKnownType::isArray);
    }

    public static Set<WellKnownType> getArrayOpenTypes(){
        return filterTypes(input -> input.isOpenType() && input.isArray());
    }

    public static Set<WellKnownType> getBufferTypes(){
        return filterTypes(WellKnownType::isBuffer);
    }

    public static WellKnownType getType(final Type t) {
        if (t == null) return null;
        else if (t instanceof WellKnownType)
            return (WellKnownType) t;
        else if (t instanceof Class<?>)
            return getType((Class<?>) t);
        else if (t instanceof TypeToken<?>)
            return getType((TypeToken<?>) t);
        else
            return null;
    }

    public static WellKnownType getArrayElementType(final ArrayType<?> arrayType){
        return arrayType != null ? getType(arrayType.getElementOpenType()) : null;
    }

    public static WellKnownType getItemType(final CompositeType type, final String itemName) {
        return getType(type.getType(itemName));
    }

    public static WellKnownType getColumnType(final TabularType type, final String itemName){
        return getItemType(type.getRowType(), itemName);
    }

    /**
     * Casts the specified value to well-known type.
     * @param value A value to cast.
     * @return Cast result
     * @throws ClassCastException Unable to cast value.
     */
    public final Object cast(final Object value) throws ClassCastException {
        return javaType.cast(value);
    }

    /**
     * Converts the specified value into well-known type using conversion rules.
     * @param value A value to convert.
     * @return Conversion result.
     * @throws ClassCastException Unable to convert value.
     */
    public abstract Object convert(final Object value) throws ClassCastException;

    @Override
    public final String toString() {
        return javaType.getCanonicalName();
    }
}
