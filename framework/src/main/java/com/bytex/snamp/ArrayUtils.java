package com.bytex.snamp;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Primitives;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 * Represents advanced routines to work with arrays.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ArrayUtils {
    private static final LoadingCache<Class<?>, Object> EMPTY_ARRAYS = CacheBuilder
            .newBuilder()
            .softValues()
            .build(new CacheLoader<Class<?>, Object>() {
                @Override
                public Object load(final Class<?> componentType) throws NegativeArraySizeException, IllegalArgumentException {
                    return Array.newInstance(componentType, 0);
                }
            });

    private static final LoadingCache<OpenType<?>, Class<?>> OPEN_TYPE_MAPPING =
            CacheBuilder.newBuilder()
                    .maximumSize(20)
                    .softValues()
                    .build(new CacheLoader<OpenType<?>, Class<?>>() {
                        @Override
                        public Class<?> load(final OpenType<?> elementType) throws ClassNotFoundException {
                            if(Objects.equals(SimpleType.BYTE, elementType))
                                return Byte.class;
                            else if(SimpleType.CHARACTER.equals(elementType))
                                return Character.class;
                            else if(SimpleType.SHORT.equals(elementType))
                                return Short.class;
                            else if(SimpleType.INTEGER.equals(elementType))
                                return Integer.class;
                            else if(SimpleType.LONG.equals(elementType))
                                return Long.class;
                            else if(SimpleType.BOOLEAN.equals(elementType))
                                return Boolean.class;
                            else if(SimpleType.FLOAT.equals(elementType))
                                return Float.class;
                            else if(SimpleType.DOUBLE.equals(elementType))
                                return Double.class;
                            else if(SimpleType.VOID.equals(elementType))
                                return Void.class;
                            else if(SimpleType.STRING.equals(elementType))
                                return String.class;
                            else if(SimpleType.BIGDECIMAL.equals(elementType))
                                return BigDecimal.class;
                            else if(SimpleType.BIGINTEGER.equals(elementType))
                                return BigInteger.class;
                            else if(SimpleType.DATE.equals(elementType))
                                return Date.class;
                            else if(SimpleType.OBJECTNAME.equals(elementType))
                                return ObjectName.class;
                            else if(elementType instanceof CompositeType)
                                return CompositeData.class;
                            else if(elementType instanceof TabularType)
                                return TabularData.class;
                            else return Class.forName(elementType.getClassName());
                        }
                    });

    private ArrayUtils(){
    }

    /**
     * Removes all empty arrays from the cache.
     */
    public static void invalidateEmptyArrays(){
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
        if (arrayType.isArray())
            return arrayType.cast(emptyArrayImpl(arrayType.getComponentType()));
        else throw new IllegalArgumentException("Invalid array type: " + arrayType);
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
     * Converts collection to array.
     * @param source The collection to convert.
     * @param componentType Array component type.
     * @param <T> Array component type.
     * @return An array with elements from the collection.
     */
    public static <T> T[] toArray(final Collection<? extends T> source, final Class<T> componentType){
        return source.toArray(ObjectArrays.newArray(componentType, source.size()));
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

    public static boolean containsAny(final Object[] array, final Object... elements) {
        if (array == null) return false;
        for (final Object actual : array)
            for (final Object expected : elements)
                if (Objects.equals(expected, actual)) return true;
        return false;
    }

    public static boolean containsAll(final Object[] array, final Object... elements) {
        if (array == null) return false;
        int counter = 0;
        for (final Object expected : elements)
            for (final Object actual : array)
                if (Objects.equals(expected, actual))
                    counter += 1;
        return counter == elements.length;
    }

    private static Object[] boxArrayImpl(final Object primitiveArray) {
        final Object[] result = ObjectArrays.newArray(Primitives.wrap(primitiveArray.getClass().getComponentType()),
                Array.getLength(primitiveArray));
        for (int i = 0; i < result.length; i++)
            result[i] = Array.get(primitiveArray, i);
        return result;
    }

    public static Byte[] boxArray(final byte[] bytes) {
        return (Byte[])boxArrayImpl(bytes);
    }

    public static Short[] boxArray(final short[] values) {
        return (Short[])boxArrayImpl(values);
    }

    public static Float[] boxArray(final float[] values) {
        return (Float[])boxArrayImpl(values);
    }

    public static Double[] boxArray(final double[] values) {
        return (Double[])boxArrayImpl(values);
    }

    public static Character[] boxArray(final char[] values) {
        return (Character[])boxArrayImpl(values);
    }

    public static Long[] boxArray(final long[] values) {
        return (Long[])boxArrayImpl(values);
    }

    public static Integer[] boxArray(final int[] value){
        return (Integer[])boxArrayImpl(value);
    }

    public static Boolean[] boxArray(final boolean[] value){
        return (Boolean[])boxArrayImpl(value);
    }

    private static Object unboxArrayImpl(final Object[] array){
        final Object result = Array.newInstance(Primitives.unwrap(array.getClass().getComponentType()), array.length);
        for(int i = 0; i < array.length; i++)
            Array.set(result, i, array[i]);
        return result;
    }

    public static byte[] unboxArray(final Byte[] value) {
        return (byte[])unboxArrayImpl(value);
    }

    public static short[] unboxArray(final Short[] value) {
        return (short[]) unboxArrayImpl(value);
    }

    public static boolean[] unboxArray(final Boolean[] value) {
        return (boolean[]) unboxArrayImpl(value);
    }

    public static int[] unboxArray(final Integer[] value) {
        return (int[]) unboxArrayImpl(value);
    }

    public static long[] unboxArray(final Long[] value){
        return (long[]) unboxArrayImpl(value);
    }

    public static float[] unboxArray(final Float[] value){
        return (float[]) unboxArrayImpl(value);
    }

    public static double[] unboxArray(final Double[] value){
        return (double[]) unboxArrayImpl(value);
    }

    public static char[] unboxArray(final Character[] value){
        return (char[]) unboxArrayImpl(value);
    }

    public static <T> T find(final T[] array, final Predicate<T> filter, final T defval) {
        for(final T item: array)
            if(filter.apply(item)) return item;
        return defval;
    }

    public static <T> T find(final T[] array, final Predicate<T> filter) {
        return find(array, filter, null);
    }

    private static Object newArray(final OpenType<?> elementType,
                                   final int[] dimensions,
                                   final boolean isPrimitive) {
        Class<?> itemType = OPEN_TYPE_MAPPING.getUnchecked(elementType);
        if (itemType == null) return null;
        else if (isPrimitive) itemType = Primitives.unwrap(itemType);
        return Array.newInstance(itemType, dimensions);
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
        if(arrayType == null)
            return null;
        else if(dimensions.length != arrayType.getDimension())
            throw new IllegalArgumentException("Actual number of dimensions doesn't match to the array type");
        else return newArray(arrayType.getElementOpenType(), dimensions, arrayType.isPrimitiveArray());
    }

    public static boolean equals(final Object array1, final Object array2){
        if(Array.getLength(array1) == Array.getLength(array2)) {
            for (int i = 0; i < Array.getLength(array1); i++)
                if (!Objects.equals(Array.get(array1, i), Array.get(array2, i))) return false;
            return true;
        }
        else return false;
    }

    @SafeVarargs
    private static <T> boolean oneOf(final T first, final T... other){
        for(final T item: other)
            if(Objects.equals(first, item)) return true;
        return false;
    }

    private static <T> ArrayType<T[]> createArrayType(final SimpleType<T> elementType) throws OpenDataException{

        final boolean primitive = oneOf(elementType,
                SimpleType.BOOLEAN,
                SimpleType.CHARACTER,
                SimpleType.BYTE,
                SimpleType.SHORT,
                SimpleType.INTEGER,
                SimpleType.LONG,
                SimpleType.FLOAT,
                SimpleType.DOUBLE);
        return new ArrayType<>(elementType, primitive);
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
    public static <T> T emptyArray(final ArrayType<T> arrayType, final ClassLoader loader){
        if(arrayType.getDimension() > 1)
            throw new IllegalArgumentException("Wrong number of dimensions: " + arrayType.getDimension());
        final Class<?> elementType;
        try{
            elementType = Class.forName(arrayType.getClassName(), true, loader).getComponentType();
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return (T)emptyArrayImpl(elementType);
    }

    /**
     * Gets the first element in the array.
     * @param array Array instance. Cannot be {@literal null}.
     * @param defval Value returned from the method if array is empty.
     * @param <T> Type of array elements.
     * @return The first element in the specified array; or default value.
     */
    public static <T> T getFirst(final T[] array, final T defval){
        return array.length > 0 ? array[0] : defval;
    }

    /**
     * Gets the first element in the array.
     * @param array Array instance. Cannot be {@literal null}.
     * @param <T> Type of array elements.
     * @return The first element in the specified array; or {@literal null}, if array is empty.
     */
    public static <T> T getFirst(final T[] array){
        return getFirst(array, null);
    }

    private static boolean isNullOrEmptyArray(final Object array){
        return array == null || Array.getLength(array) == 0;
    }

    public static boolean isNullOrEmpty(final Object[] array) {
        return isNullOrEmptyArray(array);
    }
}
