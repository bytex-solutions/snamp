package com.itworks.snamp.jmx;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.reflect.TypeToken;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Describes a well-known type that should be supported by
 * resource connector and understood by resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum  WellKnownType implements Serializable, Type {
    /**
     * Represents {@link java.lang.Void} data type.
     */
    VOID(SimpleType.VOID),

    /**
     * Represents {@link javax.management.ObjectName} data type.
     */
    OBJECT_NAME(SimpleType.OBJECTNAME),

    /**
     * Represents {@link java.lang.String} data type.
     */
    STRING(SimpleType.STRING),

    /**
     * Represents {@link java.lang.Byte} data type.
     */
    BYTE(SimpleType.BYTE),

    /**
     * Represents {@link java.lang.Character} data type.
     */
    CHAR(SimpleType.CHARACTER),

    /**
     * Represents {@link java.lang.Short} data type.
     */
    SHORT(SimpleType.SHORT),

    /**
     * Represents {@link java.lang.Integer} data type.
     */
    INT(SimpleType.INTEGER),

    /**
     * Represents {@link java.lang.Long} data type.
     */
    LONG(SimpleType.LONG),

    /**
     * Represents {@link java.lang.Boolean} data type.
     */
    BOOL(SimpleType.BOOLEAN),

    /**
     * Represents {@link java.lang.Float} data type.
     */
    FLOAT(SimpleType.FLOAT),

    /**
     * Represents {@link java.lang.Boolean} data type.
     */
    DOUBLE(SimpleType.DOUBLE),

    /**
     * Represents {@link java.util.Date} data type.
     */
    DATE(SimpleType.DATE),

    /**
     * Represents {@link java.math.BigInteger} data type.
     */
    BIG_INT(SimpleType.BIGINTEGER),

    /**
     * Represents {@link java.math.BigDecimal} data type.
     */
    BIG_DECIMAL(SimpleType.BIGDECIMAL),

    /**
     * Represents {@link java.nio.ByteBuffer} data type.
     */
    BYTE_BUFFER(ByteBuffer.class),

    /**
     * Represents {@link java.nio.CharBuffer} data type.
     */
    CHAR_BUFFER(CharBuffer.class),

    /**
     * Represents {@link java.nio.ShortBuffer} data type.
     */
    SHORT_BUFFER(ShortBuffer.class),

    /**
     * Represents {@link java.nio.IntBuffer} data type.
     */
    INT_BUFFER(IntBuffer.class),

    /**
     * Represents {@link java.nio.LongBuffer} data type.
     */
    LONG_BUFFER(LongBuffer.class),

    /**
     * Represents {@link java.nio.FloatBuffer} data type.
     */
    FLOAT_BUFFER(FloatBuffer.class),

    /**
     * Represents {@link java.nio.DoubleBuffer} data type.
     */
    DOUBLE_BUFFER(DoubleBuffer.class),

    /**
     * Represents {@code byte[]} type.
     */
    BYTE_ARRAY(SimpleType.BYTE, true),

    /**
     * Represents {@link java.lang.Byte}[] type.
     */
    WRAPPED_BYTE_ARRAY(SimpleType.BYTE, false),

    /**
     * Represents {@code char[]} type.
     */
    CHAR_ARRAY(SimpleType.CHARACTER, true),

    /**
     * Represents {@link java.lang.Character}[] type.
     */
    WRAPPED_CHAR_ARRAY(SimpleType.CHARACTER, false),

    /**
     * Represents {@code short[]} type.
     */
    SHORT_ARRAY(SimpleType.SHORT, true),

    /**
     * Represents {@link java.lang.Short}[] type.
     */
    WRAPPED_SHORT_ARRAY(SimpleType.SHORT, false),

    /**
     * Represents {@code int[]} type.
     */
    INT_ARRAY(SimpleType.INTEGER, true),

    /**
     * Represents {@link java.lang.Integer}[] type.
     */
    WRAPPED_INT_ARRAY(SimpleType.INTEGER, false),

    /**
     * Represents {@code long[]} type.
     */
    LONG_ARRAY(SimpleType.LONG, true),

    /**
     * Represents {@link java.lang.Long}[] type.
     */
    WRAPPED_LONG_ARRAY(SimpleType.LONG, false),

    /**
     * Represents {@code boolean[]} type.
     */
    BOOL_ARRAY(SimpleType.BOOLEAN, true),

    /**
     * Represents {@link java.lang.Boolean}[] type.
     */
    WRAPPED_BOOL_ARRAY(SimpleType.BOOLEAN, false),

    /**
     * Represents {@code float[]} type.
     */
    FLOAT_ARRAY(SimpleType.FLOAT, true),

    /**
     * Represents {@link java.lang.Float}[] type.
     */
    WRAPPED_FLOAT_ARRAY(SimpleType.FLOAT, false),

    /**
     * Represents {@code double[]} type.
     */
    DOUBLE_ARRAY(SimpleType.DOUBLE, true),

    /**
     * Represents {@link java.lang.Double}[] type.
     */
    WRAPPED_DOUBLE_ARRAY(SimpleType.DOUBLE, false),

    /**
     * Represents {@link java.lang.String}[] type.
     */
    STRING_ARRAY(SimpleType.STRING, false),

    /**
     * Represents {@link java.util.Date}[] type.
     */
    DATE_ARRAY(SimpleType.DATE, false),

    /**
     * Represents {@link java.math.BigInteger}[] type.
     */
    BIG_INT_ARRAY(SimpleType.BIGINTEGER, false),

    /**
     * Represents {@link java.math.BigDecimal}[] type.
     */
    BIG_DECIMAL_ARRAY(SimpleType.BIGDECIMAL, false),

    /**
     * Represents {@link javax.management.ObjectName}[] type.
     */
    OBJECT_NAME_ARRAY(SimpleType.OBJECTNAME, false),

    /**
     * Represents {@link javax.management.openmbean.CompositeData} type.
     */
    DICTIONARY(CompositeData.class),

    /**
     * Represents {@link javax.management.openmbean.CompositeData}[] type.
     */
    DICTIONARY_ARRAY(CompositeData[].class),

    /**
     * Represents {@link javax.management.openmbean.TabularData} type.
     */
    TABLE(TabularData.class),

    /**
     * Represents {@link javax.management.openmbean.TabularData}[] type.
     */
    TABLE_ARRAY(TabularData[].class)
    ;

    private static final Cache<String, WellKnownType> classNameCache =
            CacheBuilder.newBuilder().build(new CacheLoader<String, WellKnownType>() {
                @Override
                public WellKnownType load(@SuppressWarnings("NullableProblems") final String className) throws InvalidKeyException {
                    for (final WellKnownType type : values())
                        if (Objects.equals(className, type.getType().getName()))
                            return type;
                    throw new InvalidKeyException("Well-known type is not defined for class " + className);
                }
            });

    private static final Cache<TypeToken<?>, WellKnownType> typeTokenCache =
            CacheBuilder.newBuilder().build(new CacheLoader<TypeToken<?>, WellKnownType>() {
                @Override
                public WellKnownType load(@SuppressWarnings("NullableProblems") final TypeToken<?> javaType) throws InvalidKeyException {
                    if(javaType.isPrimitive())
                        return load(javaType.wrap());
                    else for(final WellKnownType type: values())
                        if(javaType.isAssignableFrom(type.getType()))
                            return type;
                    throw new InvalidKeyException("Well-known type is not defined for class " + javaType);
                }
            });

    private static final Cache<OpenType<?>, WellKnownType> openTypeCache =
            CacheBuilder.newBuilder().build(new CacheLoader<OpenType<?>, WellKnownType>() {
                @Override
                public WellKnownType load(@SuppressWarnings("NullableProblems") final OpenType<?> openType) throws InvalidKeyException {
                    if (openType instanceof CompositeType)
                        return DICTIONARY;
                    else if (openType instanceof TabularType)
                        return TABLE;
                    else for (final WellKnownType type : values())
                        if (Objects.equals(openType, type.getOpenType()))
                            return type;
                    throw new InvalidKeyException("Well-known type is not defined for class " + openType);
                }
            });
    private final OpenType<?> openType;
    private final Class<?> javaType;


    private <T> WellKnownType(final SimpleType<T> openType){
        this.openType = Objects.requireNonNull(openType, "openType is null.");
        try {
            this.javaType = Class.forName(openType.getClassName());
        } catch (final ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private <A> WellKnownType(final SimpleType<?> componentType,
                         final boolean primitive){
        try {
            this.openType = new ArrayType<A>(componentType, primitive);
            this.javaType = Class.forName(openType.getClassName());
        }
        catch (final OpenDataException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private WellKnownType(final Class<?> javaType) {
        this.openType = null;
        this.javaType = Objects.requireNonNull(javaType, "javaType is null.");
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

    /**
     * Determines whether this type is primitive.
     * <p>
     *  The following enum values recognized as array:
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
     * @return {@literal true}, if this type is primitive; otherwise, {@literal false}.
     */
    public final boolean isPrimitive(){
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
    public boolean isInstance(final Object value){
        return openType != null ? openType.isValue(value) : javaType.isInstance(value);
    }

    /**
     * Gets underlying Java type that represents this SNAMP well-known type.
     * @return The underlying Java type.
     */
    public final Class<?> getType(){
        return javaType;
    }

    /**
     * Gets underlying Java type in form of the {@link com.google.common.reflect.TypeToken}.
     * @return The underlying Java type.
     */
    public final TypeToken<?> getTypeToken(){
        return TypeToken.of(getType());
    }


    /**
     * Detects well-known SNAP type using Java class name.
     * @param className The name of the Java class.
     * @return Inferred well-known SNAMP type; or {@literal null}, if type cannot be inferred.
     * @see Class#getName()
     */
    public static WellKnownType getType(final String className) {
        return classNameCache.getIfPresent(className);
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
        return openTypeCache.getIfPresent(openType);
    }

    /**
     * Detects well-known SNAMP type of the object.
     * @param value The object which type should be inferred.
     * @return Inferred well-known SNAMP type; or {@literal null} if type cannot be detected.
     */
    public static WellKnownType getType(final Object value) {
        if(value != null)
            for(final WellKnownType type: values())
                if(type.isInstance(value))
                    return type;
        return null;
    }

    /**
     * Detects well-known SNAMP type using type token.
     * @param token Type token.
     * @return Inferred well-known SNAMP type; or {@literal null} if type cannot be detected.
     */
    public static WellKnownType getType(final TypeToken<?> token){
        return token != null ? typeTokenCache.getIfPresent(token) : null;
    }

    private static EnumSet<WellKnownType> filterTypes(final Predicate<WellKnownType> filter){
        final WellKnownType[] allTypes = values();
        final Collection<WellKnownType> types = new ArrayList<>(allTypes.length);
        for(final WellKnownType t: allTypes)
            if(filter.apply(t))
                types.add(t);
        return EnumSet.copyOf(types);
    }

    public static EnumSet<WellKnownType> getPrimitiveTypes(){
        return filterTypes(new Predicate<WellKnownType>() {
            @Override
            public boolean apply(final WellKnownType input) {
                return input.isPrimitive();
            }
        });
    }

    public static EnumSet<WellKnownType> getOpenTypes(){
        return filterTypes(new Predicate<WellKnownType>() {
            @Override
            public boolean apply(final WellKnownType input) {
                return input.isOpenType();
            }
        });
    }

    public static EnumSet<WellKnownType> getArrayTypes(){
        return filterTypes(new Predicate<WellKnownType>() {
            @Override
            public boolean apply(final WellKnownType input) {
                return input.isArray();
            }
        });
    }

    public static EnumSet<WellKnownType> getArrayOpenTypes(){
        return filterTypes(new Predicate<WellKnownType>() {
            @Override
            public boolean apply(final WellKnownType input) {
                return input.isOpenType() && input.isArray();
            }
        });
    }

    public static EnumSet<WellKnownType> getBufferTypes(){
        return filterTypes(new Predicate<WellKnownType>() {
            @Override
            public boolean apply(final WellKnownType input) {
                return input.isBuffer();
            }
        });
    }
}
