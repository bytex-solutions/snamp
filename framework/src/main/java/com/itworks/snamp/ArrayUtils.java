package com.itworks.snamp;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Represents advanced routines to work with arrays.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ArrayUtils {
    private ArrayUtils(){
    }

    /**
     * Determines whether the specified object is an array.
     * @param candidate An object to check.
     * @return {@literal true}, if the specified object is an array; otherwise, {@literal false}.
     */
    public static boolean isArray(final Object candidate){
        return candidate != null && (candidate instanceof Object[] || candidate.getClass().isArray());
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

    /**
     * Adds an element to the end of the array.
     * @param array An array to add element.
     * @param element An element to insert.
     * @param componentType Type of the resulting array. Cannot be {@literal null}.
     * @param <T> Type of the array component.
     * @return A newly created array.
     */
    public static <T> T[] addToEnd(final T[] array, final T element, final Class<T> componentType){
        return add(array, array.length, element, componentType);
    }

    /**
     * Inserts a new element into the array.
     * @param array An array to add the element.
     * @param index An index of the element to add.
     * @param element An element to insert.
     * @param componentType Type of the resulting array. Cannot be {@literal null}.
     * @param <T> Type of the array elements.
     * @return A new array that contains inserted element.
     */
    public static <T> T[] add(final T[] array, final int index, final T element,
                              final Class<T> componentType) {
        if (array == null) {
            if (index != 0)
                throw createIndexOutOfBoundsException(index, 0);
            final T[] joinedArray = ObjectArrays.newArray(componentType, 1);
            joinedArray[0] = element;
            return joinedArray;
        }
        final int length = array.length;
        if (index > length || index < 0)
            throw createIndexOutOfBoundsException(index, length);
        final T[] result = ObjectArrays.newArray(componentType, length + 1);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        if (index < length)
            System.arraycopy(array, index, result, index + 1, length - index);
        return result;
    }

    public static boolean contains(final Object[] array, final Object element){
        if(array == null) return false;
        for(final Object item: array)
            if(Objects.equals(element, item)) return true;
        return false;
    }

    public static Byte[] boxArray(final byte[] bytes) {
        final Byte[] result = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            result[i] = bytes[i];
        return result;
    }

    public static byte[] unboxArray(final Byte[] value) {
        final byte[] result = new byte[value.length];
        for(int i = 0; i < value.length; i++)
            result[i] = value[i];
        return result;
    }

    public static int[] unboxArray(final Integer[] value){
        final int[] result = new int[value.length];
        for(int i = 0; i < value.length; i++)
            result[i] = value[i];
        return result;
    }

    public static Integer[] boxArray(final int[] value){
        final Integer[] result = new Integer[value.length];
        for (int i = 0; i < value.length; i++)
            result[i] = value[i];
        return result;
    }

    public static <T> T[] createAndFill(final Class<T> componentType, final T value, final int length){
        final T[] result = ObjectArrays.newArray(componentType, length);
        Arrays.fill(result, value);
        return result;
    }

    public static <T> T find(final T[] array, final Predicate<T> filter, final T defval) {
        for(final T item: array)
            if(filter.apply(item)) return item;
        return defval;
    }

    public static <T> T find(final T[] array, final Predicate<T> filter) {
        return find(array, filter, null);
    }

    public static <T> T[] filter(final T[] array, final Predicate<T> filter, final Class<T> elementType){
        final ArrayList<T> result = Lists.newArrayListWithExpectedSize(array.length);
        for(final T item: array)
            if(filter.apply(item)) result.add(item);
        return toArray(result, elementType);
    }



    private static Object newArray(final OpenType<?> elementType,
                                   final int[] dimensions,
                                   final boolean isPrimitive) throws ReflectionException {
        if(Objects.equals(SimpleType.BYTE, elementType))
            return Array.newInstance(isPrimitive ? byte.class : Byte.class, dimensions);
        else if(SimpleType.CHARACTER.equals(elementType))
            return Array.newInstance(isPrimitive ? char.class : Character.class, dimensions);
        else if(SimpleType.SHORT.equals(elementType))
            return Array.newInstance(isPrimitive ? short.class : Short.class, dimensions);
        else if(SimpleType.INTEGER.equals(elementType))
            return Array.newInstance(isPrimitive ? int.class : Integer.class, dimensions);
        else if(SimpleType.LONG.equals(elementType))
            return Array.newInstance(isPrimitive ? long.class : Long.class, dimensions);
        else if(SimpleType.BOOLEAN.equals(elementType))
            return Array.newInstance(isPrimitive ? boolean.class : Boolean.class, dimensions);
        else if(SimpleType.FLOAT.equals(elementType))
            return Array.newInstance(isPrimitive ? float.class : Float.class, dimensions);
        else if(SimpleType.DOUBLE.equals(elementType))
            return Array.newInstance(isPrimitive ? double.class : Double.class, dimensions);
        else if(SimpleType.VOID.equals(elementType))
            return Array.newInstance(isPrimitive ? void.class : Void.class, dimensions);
        else if(SimpleType.STRING.equals(elementType))
            return Array.newInstance(String.class, dimensions);
        else if(SimpleType.BIGDECIMAL.equals(elementType))
            return Array.newInstance(BigDecimal.class, dimensions);
        else if(SimpleType.BIGINTEGER.equals(elementType))
            return Array.newInstance(BigInteger.class, dimensions);
        else if(SimpleType.DATE.equals(elementType))
            return Array.newInstance(Date.class, dimensions);
        else if(SimpleType.OBJECTNAME.equals(elementType))
            return Array.newInstance(ObjectName.class, dimensions);
        else if(elementType instanceof CompositeType)
            return Array.newInstance(CompositeData.class, dimensions);
        else if(elementType instanceof TabularType)
            return Array.newInstance(TabularData.class, dimensions);
        else try{
            return Array.newInstance(Class.forName(elementType.getClassName()), dimensions);
        }
        catch (final ClassNotFoundException e){
            throw new ReflectionException(e);
        }
    }

    /**
     * Creates a new instance of the array.
     * @param arrayType An array type definition.
     * @param dimensions An array of length of each dimension.
     * @return A new empty array.
     * @throws ReflectionException Unable to create a new array.
     * @throws java.lang.IllegalArgumentException The specified number of dimensions doesn't match to the number of dimensions
     * in the array definition.
     */
    public static Object newArray(final ArrayType<?> arrayType, final int... dimensions) throws ReflectionException {
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

    public static <T> T[] emptyIfNull(final T[] items, final Class<T> elementType) {
        return items == null ? ObjectArrays.newArray(elementType, 0) : items;
    }
}
