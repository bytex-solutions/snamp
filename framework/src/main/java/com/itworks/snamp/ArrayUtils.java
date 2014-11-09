package com.itworks.snamp;

import java.lang.reflect.Array;
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
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(final Collection<T> source, final Class<T> componentType){
        return source.toArray((T[])Array.newInstance(componentType, source.size()));
    }

    public static <I, O extends I> O castArrayElement(final I[] array,
                                                      final int index,
                                                      final Class<O> elementType,
                                                      final O defval){
        final I element = array[index];
        return elementType.isInstance(element) ? elementType.cast(element) : defval;
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
    public static <T> T[] remove(final T[] array, final int index){
        return (T[]) remove((Object) array, index);
    }

    private static IndexOutOfBoundsException createIndexOutOfBoundsException(final int index, final int length){
        return new IndexOutOfBoundsException(String.format("Index: %s, Length: %s", index, length));
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static Object remove(final Object array, final int index) throws IndexOutOfBoundsException{
        if(array == null) return null;
        final int length = Array.getLength(array);
        if (index < 0 || index >= length)
            throw createIndexOutOfBoundsException(index, length);
        final Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1)
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        return result;
    }

    /**
     * Adds an element to the end of the array.
     * @param array An array to add element.
     * @param element An element to insert.
     * @param <T> Type of the array component.
     * @return A newly created array.
     */
    public static <T> T[] addToEnd(final T[] array, final T element){
        return add(array, array.length, element);
    }

    /**
     * Inserts a new element into the array.
     * @param array An array to add the element.
     * @param index An index of the element to add.
     * @param element An element to insert.
     * @param <T> Type of the array elements.
     * @return A new array that contains inserted element.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(final T[] array, final int index, final T element) {
        final Class<?> componentType;
        if (array != null)
            componentType = array.getClass().getComponentType();
        else if (element != null)
            componentType = element.getClass();
        else
            throw new IllegalArgumentException("Array and element cannot both be null");
        return  (T[]) add(array, index, element, componentType);
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static Object add(final Object array, final int index, final Object element, final Class<?> clss) {
        if (array == null) {
            if (index != 0)
                throw createIndexOutOfBoundsException(index, 0);
            final Object joinedArray = Array.newInstance(clss, 1);
            Array.set(joinedArray, 0, element);
            return joinedArray;
        }
        final int length = Array.getLength(array);
        if (index > length || index < 0)
            throw createIndexOutOfBoundsException(index, length);
        final Object result = Array.newInstance(clss, length + 1);
        System.arraycopy(array, 0, result, 0, index);
        Array.set(result, index, element);
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
