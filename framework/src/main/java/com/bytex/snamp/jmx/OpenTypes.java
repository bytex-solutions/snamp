package com.bytex.snamp.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazyReference;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;

import javax.annotation.Nonnull;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents binding between {@link OpenType} and {@link Class}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class OpenTypes {
    private static final LoadingCache<OpenType<?>, Class<?>> OPEN_TYPE_MAPPING = CacheBuilder.newBuilder()
            .maximumSize(20)
            .softValues()
            .build(new CacheLoader<OpenType<?>, Class<?>>() {
                @Override
                public Class<?> load(@Nonnull final OpenType<?> openType) throws ClassNotFoundException {
                    return Class.forName(openType.getClassName(), true, getClass().getClassLoader());
                }
            });

    private static final LazyReference<Set<SimpleType<?>>> PRIMITIVE_TYPES = LazyReference.soft();
    private static final LazyReference<Map<SimpleType<?>, ?>> DEFAULT_VALUES = LazyReference.soft();

    private OpenTypes() {
        throw new InstantiationError();
    }

    private static ImmutableSet<SimpleType<?>> loadPrimitiveTypes() {
        return ImmutableSet.of(SimpleType.BOOLEAN,
                SimpleType.CHARACTER,
                SimpleType.BYTE,
                SimpleType.SHORT,
                SimpleType.INTEGER,
                SimpleType.LONG,
                SimpleType.FLOAT,
                SimpleType.DOUBLE);
    }

    private static ImmutableMap<SimpleType<?>, Comparable<?>> loadDefaultValues() {
        final class DefaultValues extends HashMap<SimpleType<?>, Comparable<?>> {
            private static final long serialVersionUID = -2003860346504227876L;

            //macros for type safety
            private <T extends Comparable<T>> void add(final SimpleType<T> type, final T value) {
                put(type, value);
            }
        }

        final DefaultValues values = new DefaultValues();
        values.add(SimpleType.BOOLEAN, Boolean.FALSE);
        values.add(SimpleType.CHARACTER, '\0');
        values.add(SimpleType.BYTE, (byte) 0);
        values.add(SimpleType.SHORT, (short) 0);
        values.add(SimpleType.INTEGER, 0);
        values.add(SimpleType.LONG, 0L);
        values.add(SimpleType.FLOAT, 0F);
        values.add(SimpleType.DOUBLE, 0D);
        values.add(SimpleType.STRING, "");
        values.add(SimpleType.DATE, new Date(0));
        values.add(SimpleType.OBJECTNAME, callUnchecked(() -> new ObjectName("")));  //constructor never throws exception
        values.add(SimpleType.BIGINTEGER, BigInteger.ZERO);
        values.add(SimpleType.BIGDECIMAL, BigDecimal.ZERO);
        return ImmutableMap.copyOf(values);
    }

    /**
     * Gets underlying class associated with the specified {@link OpenType}.
     *
     * @param type Type to resolve.
     * @param <T>  Underlying type.
     * @return Metadata of underlying class.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getType(@Nonnull final OpenType<T> type) {
        return (Class<T>) OPEN_TYPE_MAPPING.getUnchecked(type);
    }

    /**
     * Determines whether the specified type can be interpreted as primitive type in Java language.
     *
     * @param type The type to check.
     * @return {@literal true}, if the specified type is primitive type in Java language; otherwise, {@literal false}.
     */
    public static boolean isPrimitive(@Nonnull final SimpleType<?> type) {
        return PRIMITIVE_TYPES.lazyGet(OpenTypes::loadPrimitiveTypes).contains(type);
    }

    /**
     * Casts value to the specified JMX Open Type.
     * @param <T> Type of the conversion result.
     * @param value Value to cast.
     * @param type JMX Open Type. Cannot be {@literal null}.
     * @return Converter value.
     */
    public static <T> Optional<T> convert(final Object value, final OpenType<T> type){
        return Convert.toType(value, type);
    }

    /**
     * Gets default value of the specified OpenType.
     *
     * @param type OpenType instance. Cannot be {@literal null}.
     * @param <T>  Type of the value.
     * @return Default value of the specified type; or {@literal null}, if no default value is specified.
     */
    public static <T> T defaultValue(final OpenType<T> type) {
        final Object result;
        if (type instanceof CompositeType) {
            final CompositeType ctype = (CompositeType) type;
            final Map<String, Object> items = ctype.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), itemName -> defaultValue(ctype.getType(itemName))));
            result = callUnchecked(() -> new CompositeDataSupport(ctype, items));
        } else if (type instanceof TabularType)
            result = new TabularDataSupport((TabularType) type);
        else if (type instanceof ArrayType<?>)
            return ArrayUtils.emptyArray(getType(type));
        else if (type instanceof SimpleType<?>)
            result = DEFAULT_VALUES.lazyGet(OpenTypes::loadDefaultValues).get(type);
        else
            result = null;
        return convert(result, type).orElse(null);
    }

    private static Object newArray(final OpenType<?> elementType,
                                   final int[] dimensions,
                                   final boolean isPrimitive) {
        final Class<?> itemType = getType(elementType);
        return Array.newInstance(isPrimitive ? Primitives.unwrap(itemType) : itemType, dimensions);
    }

    /**
     * Creates a new instance of the array.
     *
     * @param arrayType  An array type definition.
     * @param dimensions An array of length of each dimension.
     * @return A new empty array.
     * @throws java.lang.IllegalArgumentException The specified number of dimensions doesn't match to the number of dimensions
     *                                            in the array definition.
     */
    public static <T> T newArray(final ArrayType<T> arrayType, final int... dimensions) {
        if (arrayType == null || dimensions == null)
            return null;
        else if (dimensions.length != arrayType.getDimension())
            throw new IllegalArgumentException("Actual number of dimensions doesn't match to the array type");
        else {
            final Object array = newArray(arrayType.getElementOpenType(), dimensions, arrayType.isPrimitiveArray());
            return convert(array, arrayType).orElseThrow(AssertionError::new);
        }
    }

    private static <T> ArrayType<T[]> createArrayType(final SimpleType<T> elementType) throws OpenDataException{
        return new ArrayType<>(elementType, isPrimitive(elementType));
    }

    public static <T> ArrayType<T[]> createArrayType(final OpenType<T> elementType) throws OpenDataException {
        return elementType instanceof SimpleType<?> ? createArrayType((SimpleType<T>) elementType) : ArrayType.getArrayType(elementType);
    }
}
