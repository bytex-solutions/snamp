package com.itworks.snamp;

import com.google.common.collect.ObjectArrays;

import java.util.Collection;
import java.util.Objects;

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
     * Converts collection to array.
     * @param source The collection to convert.
     * @param componentType Array component type.
     * @param <T> Array component type.
     * @return An array with elements from the collection.
     */
    public static <T> T[] toArray(final Collection<T> source, final Class<T> componentType){
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
}
