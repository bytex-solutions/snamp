package com.bytex.snamp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.*;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents advanced routines to work with arrays.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public final class ArrayUtils {
    @FunctionalInterface
    private interface ToByteArrayConverter<T>{
        byte[] convert(final T array, final int index);
    }

    private static final ImmutableSet<SimpleType<?>> PRIMITIVE_TYPES = ImmutableSet.of(SimpleType.BOOLEAN,
            SimpleType.CHARACTER,
            SimpleType.BYTE,
            SimpleType.SHORT,
            SimpleType.INTEGER,
            SimpleType.LONG,
            SimpleType.FLOAT,
            SimpleType.DOUBLE);

    private static final LoadingCache<Class<?>, Object> EMPTY_ARRAYS = CacheBuilder
            .newBuilder()
            .softValues()
            .build(new CacheLoader<Class<?>, Object>() {
                @Override
                public Object load(@Nonnull final Class<?> componentType) throws IllegalArgumentException, NegativeArraySizeException {
                    return Array.newInstance(componentType, 0);
                }
            });

    private static final LoadingCache<OpenType<?>, Class<?>> OPEN_TYPE_MAPPING =
            CacheBuilder.newBuilder()
                    .maximumSize(20)
                    .softValues()
                    .build(new CacheLoader<OpenType<?>, Class<?>>() {
                        private final Map<OpenType<?>, Class<?>> simpleTypeMapping = ImmutableMap.<OpenType<?>, Class<?>>builder()
                                .put(SimpleType.BYTE, Byte.class)
                                .put(SimpleType.CHARACTER, Character.class)
                                .put(SimpleType.SHORT, Short.class)
                                .put(SimpleType.INTEGER, Integer.class)
                                .put(SimpleType.LONG, Long.class)
                                .put(SimpleType.BOOLEAN, Boolean.class)
                                .put(SimpleType.FLOAT, Float.class)
                                .put(SimpleType.DOUBLE, Double.class)
                                .put(SimpleType.VOID, Void.class)
                                .put(SimpleType.STRING, String.class)
                                .put(SimpleType.BIGDECIMAL, BigDecimal.class)
                                .put(SimpleType.BIGINTEGER, BigInteger.class)
                                .put(SimpleType.OBJECTNAME, ObjectName.class)
                                .put(SimpleType.DATE, Date.class)
                                .build();

                        @Override
                        public Class<?> load(@Nonnull final OpenType<?> elementType) throws ClassNotFoundException {
                            if(elementType instanceof CompositeType)
                                return CompositeData.class;
                            else if(elementType instanceof TabularType)
                                return TabularData.class;
                            else {
                                final Class<?> result = simpleTypeMapping.get(elementType);
                                return result == null ? Class.forName(elementType.getClassName()) : result;
                            }
                        }
                    });

    private ArrayUtils(){
        throw new InstantiationError();
    }

    /**
     * Removes all empty arrays from the cache.
     */
    static void invalidateEmptyArrays(){
        EMPTY_ARRAYS.invalidateAll();
    }

    private static Object emptyArrayImpl(final Class<?> componentType){
        return EMPTY_ARRAYS.getUnchecked(componentType);
    }

    /**
     * Creates an empty array of the specified type.
     * @param arrayType Array type. Cannot be {@literal null}.
     * @param <T> Array type.
     * @return Empty array.
     */
    public static <T> T emptyArray(final Class<T> arrayType) {
        final Class<?> componentType = arrayType.getComponentType();
        if (componentType == null)
            throw new IllegalArgumentException("Invalid array type: " + arrayType);
        else
            return arrayType.cast(emptyArrayImpl(componentType));
    }

    /**
     * Gets cached empty array of bytes.
     * @return Cached empty array of bytes.
     * @since 2.0
     */
    public static byte[] emptyByteArray(){
        return emptyArray(byte[].class);
    }

    /**
     * Determines whether the specified object is an array.
     * @param candidate An object to check.
     * @return {@literal true}, if the specified object is an array; otherwise, {@literal false}.
     */
    public static boolean isArray(final Object candidate){
        return candidate instanceof Object[] || candidate != null && candidate.getClass().isArray();
    }

    /**
     * Removes an element from an array.
     * @param array An array to remove the element.
     * @param index An index of the element to remove.
     * @param <T> Type of the array elements.
     * @return A new array containing the existing elements except the element
     * at the specified position.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(final T[] array, final int index) {
        return (T[]) removeImpl(array, index);
    }

    private static IndexOutOfBoundsException createIndexOutOfBoundsException(final int index, final int length){
        return new IndexOutOfBoundsException(String.format("Index: %s, Length: %s", index, length));
    }

    private static Object[] removeImpl(final Object[] array, final int index) throws IndexOutOfBoundsException {
        if (array == null) return null;
        final int length = array.length;
        if (index < 0 || index >= length)
            throw createIndexOutOfBoundsException(index, length);
        final Object[] result = ObjectArrays.newArray(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1)
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        return result;
    }

    public static boolean contains(final Object[] array, final Object element) {
        return find(array, element::equals).isPresent();
    }

    private static Object[] wrapArrayImpl(final Object primitiveArray) {
        final Object[] result = ObjectArrays.newArray(Primitives.wrap(primitiveArray.getClass().getComponentType()),
                Array.getLength(primitiveArray));
        for (int i = 0; i < result.length; i++)
            result[i] = Array.get(primitiveArray, i);
        return result;
    }

    public static Byte[] wrapArray(final byte[] bytes) {
        return (Byte[]) wrapArrayImpl(bytes);
    }

    public static Short[] wrapArray(final short[] values) {
        return (Short[]) wrapArrayImpl(values);
    }

    public static Float[] wrapArray(final float[] values) {
        return (Float[]) wrapArrayImpl(values);
    }

    public static Double[] wrapArray(final double[] values) {
        return (Double[]) wrapArrayImpl(values);
    }

    public static Character[] wrapArray(final char[] values) {
        return (Character[]) wrapArrayImpl(values);
    }

    public static Long[] wrapArray(final long[] values) {
        return (Long[]) wrapArrayImpl(values);
    }

    public static Integer[] wrapArray(final int[] value){
        return (Integer[]) wrapArrayImpl(value);
    }

    public static Boolean[] wrapArray(final boolean[] value){
        return (Boolean[]) wrapArrayImpl(value);
    }

    private static Object unwrapArrayImpl(final Object[] array){
        final Object result = Array.newInstance(Primitives.unwrap(array.getClass().getComponentType()), array.length);
        for(int i = 0; i < array.length; i++)
            Array.set(result, i, array[i]);
        return result;
    }

    public static byte[] unwrapArray(final Byte[] value) {
        return (byte[]) unwrapArrayImpl(value);
    }

    public static short[] unwrapArray(final Short[] value) {
        return (short[]) unwrapArrayImpl(value);
    }

    public static boolean[] unwrapArray(final Boolean[] value) {
        return (boolean[]) unwrapArrayImpl(value);
    }

    public static int[] unwrapArray(final Integer[] value) {
        return (int[]) unwrapArrayImpl(value);
    }

    public static long[] unwrapArray(final Long[] value){
        return (long[]) unwrapArrayImpl(value);
    }

    public static float[] unwrapArray(final Float[] value){
        return (float[]) unwrapArrayImpl(value);
    }

    public static double[] unwrapArray(final Double[] value){
        return (double[]) unwrapArrayImpl(value);
    }

    public static char[] unwrapArray(final Character[] value){
        return (char[]) unwrapArrayImpl(value);
    }

    public static <T> Optional<T> find(final T[] array, final Predicate<T> filter) {
        for(final T item: array)
            if(filter.test(item)) return Optional.ofNullable(item);
        return Optional.empty();
    }

    private static Object newArray(final OpenType<?> elementType,
                                   final int[] dimensions,
                                   final boolean isPrimitive) {
        final Class<?> itemType = OPEN_TYPE_MAPPING.getUnchecked(elementType);
        return Array.newInstance(isPrimitive ? Primitives.unwrap(itemType) : itemType, dimensions);
    }

    /**
     * Creates a new instance of the array.
     * @param arrayType An array type definition.
     * @param dimensions An array of length of each dimension.
     * @return A new empty array.
     * @throws java.lang.IllegalArgumentException The specified number of dimensions doesn't match to the number of dimensions
     * in the array definition.
     */
    public static Object newArray(final ArrayType<?> arrayType, final int... dimensions) {
        if(arrayType == null || dimensions == null)
            return null;
        else if(dimensions.length != arrayType.getDimension())
            throw new IllegalArgumentException("Actual number of dimensions doesn't match to the array type");
        else return newArray(arrayType.getElementOpenType(), dimensions, arrayType.isPrimitiveArray());
    }

    public static boolean equals(final Object array1, final Object array2){
        return equals(array1, array2, false);
    }

    public static boolean strictEquals(final Object array1, final Object array2){
        return equals(array1, array2, true);
    }

    private static boolean equals(final Object array1, final Object array2, final boolean strictComponentType) {
        if (strictComponentType && !array1.getClass().getComponentType().equals(array2.getClass().getComponentType()))
            return false;
        else if (Array.getLength(array1) == Array.getLength(array2)) {
            for (int i = 0; i < Array.getLength(array1); i++)
                if (!Objects.equals(Array.get(array1, i), Array.get(array2, i))) return false;
            return true;
        } else
            return false;
    }

    private static <T> ArrayType<T[]> createArrayType(final SimpleType<T> elementType) throws OpenDataException{
        return new ArrayType<>(elementType, PRIMITIVE_TYPES.contains(elementType));
    }

    public static <T> ArrayType<T[]> createArrayType(final OpenType<T> elementType) throws OpenDataException {
        if(elementType instanceof SimpleType<?>)
            return createArrayType((SimpleType<T>)elementType);
        else return ArrayType.getArrayType(elementType);
    }

    /**
     * Gets empty array of the specified type.
     * @param arrayType An array type. Must be single-dimensional. Cannot be {@literal null}.
     * @param loader Class loader used to resolve component type of array. May be {@literal null}.
     * @param <T> Type of elements in the array.
     * @return Empty array.
     * @throws IllegalArgumentException Incorrect array type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T emptyArray(final ArrayType<T> arrayType, final ClassLoader loader) {
        if (arrayType.getDimension() > 1)
            throw new IllegalArgumentException("Wrong number of dimensions: " + arrayType.getDimension());
        final Class<?> elementType = callAndWrapException(
                () -> Class.forName(arrayType.getClassName(), true, loader).getComponentType(),
                IllegalArgumentException::new);
        return (T) emptyArrayImpl(elementType);
    }

    public static <T> Optional<T> getLast(final T[] array){
        return array.length > 0 ? Optional.ofNullable(array[array.length - 1]) : Optional.empty();
    }

    /**
     * Gets the first element in the array.
     * @param array Array instance. Cannot be {@literal null}.
     * @param <T> Type of array elements.
     * @return The first element in the specified array; or default value.
     */
    public static <T> Optional<T> getFirst(final T[] array) {
        return isNullOrEmpty(array) ? Optional.empty() : Optional.ofNullable(array[0]);
    }

    private static boolean isNullOrEmptyImpl(final Object array){
        return array == null || Array.getLength(array) == 0;
    }

    public static boolean isNullOrEmpty(final Object[] array) {
        return isNullOrEmptyImpl(array);
    }

    public static boolean isNullOrEmpty(final byte[] array){
        return isNullOrEmptyImpl(array);
    }

    private static <T extends Comparable<T> & Serializable> Object toArray(final byte[] array,
                                                                           Class<T> newElementType,
                                                                           final Function<byte[], T> converter,
                                                                           final int componentSize,
                                                                           final boolean primitive) {
        if (primitive) newElementType = Primitives.unwrap(newElementType);
        final Object result = Array.newInstance(newElementType, array.length / componentSize);
        for (int sourcePosition = 0, destPosition = 0; sourcePosition < array.length; sourcePosition += componentSize, destPosition += 1) {
            final byte[] subbuffer = new byte[componentSize];
            if (array.length - sourcePosition < componentSize)
                return result;
            else {
                System.arraycopy(array, sourcePosition, subbuffer, 0, componentSize);
                Array.set(result, destPosition, converter.apply(subbuffer));
            }
        }
        return result;
    }

    private static Object toShortArray(final byte[] array, final boolean primitive){
        return toArray(array, Short.class, Shorts::fromByteArray, Short.BYTES, primitive);
    }

    public static short[] toShortArray(final byte[] array){
        return (short[])toShortArray(array, true);
    }

    public static Short[] toWrappedShortArray(final byte[] array){
        return (Short[])toShortArray(array, false);
    }

    private static Object toIntArray(final byte[] array, final boolean primitive){
        return toArray(array, Integer.class, Ints::fromByteArray, Integer.BYTES, primitive);
    }

    public static int[] toIntArray(final byte[] array){
        return (int[])toIntArray(array, true);
    }

    public static Integer[] toWrappedIntArray(final byte[] array){
        return (Integer[])toIntArray(array, false);
    }

    private static Object toLongArray(final byte[] array, final boolean primitive){
        return toArray(array, Long.class, Longs::fromByteArray, Long.BYTES, primitive);
    }

    public static long[] toLongArray(final byte[] array){
        return (long[])toLongArray(array, true);
    }

    public static Long[] toWrappedLongArray(final byte[] array){
        return (Long[])toLongArray(array, false);
    }

    private static Object toFloatArray(final byte[] array, final boolean primitive){
        return toArray(array, Float.class, input -> {
            final int bits = Ints.fromByteArray(input);
            return Float.intBitsToFloat(bits);
        }, Float.BYTES, primitive);
    }

    public static float[] toFloatArray(final byte[] array){
        return (float[])toFloatArray(array, true);
    }

    public static Float[] toWrappedFloatArray(final byte[] array){
        return (Float[])toFloatArray(array, false);
    }

    private static Object toDoubleArray(final byte[] array, final boolean primitive){
        return toArray(array, Double.class, input -> {
            final long bits = Longs.fromByteArray(input);
            return Double.longBitsToDouble(bits);
        }, Double.BYTES, primitive);
    }

    public static double[] toDoubleArray(final byte[] array){
        return (double[])toDoubleArray(array, true);
    }

    public static Double[] toWrappedDoubleArray(final byte[] array){
        return (Double[])toDoubleArray(array, false);
    }

    private static Object toCharArray(final byte[] array, final boolean primitive){
        return toArray(array, Character.class, Chars::fromByteArray, Character.BYTES, primitive);
    }

    public static char[] toCharArray(final byte[] array){
        return (char[])toCharArray(array, true);
    }

    public static Character[] toWrappedCharArray(final byte[] array){
        return (Character[])toCharArray(array, false);
    }

    private static Object toBoolArray(final byte[] array, final boolean primitive){
        final BitSet bits = BitSet.valueOf(array);
        final Object result = Array.newInstance(primitive ? boolean.class : Boolean.class, bits.length());
        for(int i = 0; i < bits.length(); i++)
            Array.set(result, i, bits.get(i));
        return result;
    }

    public static boolean[] toBoolArray(final byte[] array){
        return (boolean[])toBoolArray(array, true);
    }

    public static Boolean[] toWrappedBoolArray(final byte[] array){
        return (Boolean[])toBoolArray(array, false);
    }

    private static <T> byte[] toByteArray(final T array,
                                          final ToByteArrayConverter<T> converter,
                                          final int componentSize) {
        final int arrayLen = Array.getLength(array);
        final byte[] result = new byte[arrayLen * componentSize];
        for (int sourcePosition = 0, destPosition = 0; sourcePosition < arrayLen; sourcePosition++) {
            final byte[] subArray = converter.convert(array, sourcePosition);
            assert subArray.length == componentSize;
            System.arraycopy(subArray, 0, result, destPosition, subArray.length);
            destPosition += subArray.length;
        }
        return result;
    }

    public static byte[] toByteArray(final short[] value) {
        return toByteArray(value, (value1, index) -> Shorts.toByteArray(value1[index]), Short.BYTES);
    }

    public static byte[] toByteArray(final Short[] value) {
        return toByteArray(value, (value1, index) -> Shorts.toByteArray(value1[index]), Short.BYTES);
    }

    public static byte[] toByteArray(final int[] value) {
        return toByteArray(value, (value1, index) -> Ints.toByteArray(value1[index]), Integer.BYTES);
    }

    public static byte[] toByteArray(final Integer[] value) {
        return toByteArray(value, (value1, index) -> Ints.toByteArray(value1[index]), Integer.BYTES);
    }

    public static byte[] toByteArray(final long[] value) {
        return toByteArray(value, (value1, index) -> Longs.toByteArray(value1[index]), Long.BYTES);
    }

    public static byte[] toByteArray(final Long[] value) {
        return toByteArray(value, (value1, index) -> Longs.toByteArray(value1[index]), Long.BYTES);
    }

    public static byte[] toByteArray(final float[] value) {
        return toByteArray(value, (value1, index) -> Ints.toByteArray(Float.floatToIntBits(value1[index])), Float.BYTES);
    }

    public static byte[] toByteArray(final Float[] value) {
        return toByteArray(value, (value1, index) -> Ints.toByteArray(Float.floatToIntBits(value1[index])), Float.BYTES);
    }

    public static byte[] toByteArray(final double[] value) {
        return toByteArray(value, (value1, index) -> Longs.toByteArray(Double.doubleToLongBits(value1[index])), Double.BYTES);
    }

    public static byte[] toByteArray(final Double[] value) {
        return toByteArray(value, (value1, index) -> Longs.toByteArray(Double.doubleToLongBits(value1[index])), Double.BYTES);
    }

    public static byte[] toByteArray(final char[] value) {
        return toByteArray(value, (value1, index) -> Chars.toByteArray(value1[index]), Character.BYTES);
    }

    public static byte[] toByteArray(final Character[] value) {
        return toByteArray(value, (value1, index) -> Chars.toByteArray(value1[index]), Character.BYTES);
    }

    public static byte[] toByteArray(final boolean[] value) {
        final BitSet result = new BitSet(value.length);
        for(int index = 0; index < value.length; index++)
            result.set(index, value[index]);
        return result.toByteArray();
    }

    public static byte[] toByteArray(final Boolean[] value) {
        final BitSet result = new BitSet(value.length);
        for(int index = 0; index < value.length; index++)
            result.set(index, value[index]);
        return result.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> IntFunction<T[]> arrayConstructor(final Class<T> elementType) {
        return length -> length == 0 ? (T[]) emptyArrayImpl(elementType) : ObjectArrays.newArray(elementType, length);
    }

    public static <I, O> O[] transform(final I[] array, final Class<O> elementType, final Function<? super I, ? extends O> transformer){
        final O[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformByteArray(final byte[] array, final Class<T> elementType, final IntFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformShortArray(final short[] array, final Class<T> elementType, final IntFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformIntArray(final int[] array, final Class<T> elementType, final IntFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformCharArray(final char[] array, final Class<T> elementType, final IntFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformLongArray(final long[] array, final Class<T> elementType, final LongFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformFloatArray(final float[] array, final Class<T> elementType, final DoubleFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformDoubleArray(final double[] array, final Class<T> elementType, final DoubleFunction<? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }

    public static <T> T[] transformBooleanArray(final boolean[] array, final Class<T> elementType, final Function<? super Boolean, ? extends T> transformer){
        final T[] result = ObjectArrays.newArray(elementType, array.length);
        for(int index = 0; index < array.length; index++)
            result[index] = transformer.apply(array[index]);
        return result;
    }
}
