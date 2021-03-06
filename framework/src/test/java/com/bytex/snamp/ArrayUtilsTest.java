package com.bytex.snamp;

import com.google.common.primitives.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ArrayUtilsTest extends Assert {
    @Test
    public void emptyArrayTest(){
        final String[] array = ArrayUtils.emptyArray(String[].class);
        assertNotNull(array);
        assertTrue(array.length == 0);
        System.gc();
        assertEquals(System.identityHashCode(array),
                System.identityHashCode(ArrayUtils.emptyArray(String[].class)));
    }

    @Test
    public void invalidateTest(){
        final String[] array = ArrayUtils.emptyArray(String[].class);
        assertNotNull(array);
        assertTrue(array.length == 0);
        ArrayUtils.invalidateEmptyArrays();
        System.gc();
        assertNotEquals(System.identityHashCode(array),
                System.identityHashCode(ArrayUtils.emptyArray(String[].class)));
    }

    @Test
    public void boxArrayTest(){
        Byte[] result = ArrayUtils.wrapArray(new byte[]{2, 5, 7});
        assertEquals(3, result.length);
        assertEquals(new Byte((byte)2), result[0]);
        assertEquals(new Byte((byte)5), result[1]);
        assertEquals(new Byte((byte)7), result[2]);
        result = ArrayUtils.wrapArray(new byte[0]);
        assertEquals(0, result.length);
    }

    @Test
    public void unboxArrayTest(){
        int[] result = ArrayUtils.unwrapArray(new Integer[]{2, 5, 7});
        assertEquals(3, result.length);
        assertEquals(2, result[0]);
        assertEquals(5, result[1]);
        assertEquals(7, result[2]);
        result = ArrayUtils.unwrapArray(new Integer[0]);
        assertEquals(0, result.length);
    }

    @Test
    public void toShortArrayTest(){
        final short[] array1 = ArrayUtils.toShortArray(new byte[]{0, 10, 0, 20, 30});
        assertArrayEquals(new short[]{10, 20}, array1);
        final Short[] array2 = ArrayUtils.toWrappedShortArray(new byte[]{0, 10, 0, 20, 30});
        assertArrayEquals(new Short[]{10, 20}, array2);
    }

    @Test
    public void toIntArrayTest(){
        final int[] array1 = ArrayUtils.toIntArray(new byte[]{0, 0, 0, 10, 0, 0, 0, 20, 30});
        assertArrayEquals(new int[]{10, 20}, array1);
        final Integer[] array2 = ArrayUtils.toWrappedIntArray(new byte[]{0, 0, 0, 10, 0, 0, 0, 20, 30});
        assertArrayEquals(new Integer[]{10, 20}, array2);
    }

    @Test
    public void toLongArrayTest(){
        final long[] array1 = ArrayUtils.toLongArray(new byte[]{0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 20});
        assertArrayEquals(new long[]{10L, 20L}, array1);
        final Long[] array2 = ArrayUtils.toWrappedLongArray(new byte[]{0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 20});
        assertArrayEquals(new Long[]{10L, 20L}, array2);
    }

    @Test
    public void toFloatArrayTest(){
        final int first = Float.floatToIntBits(10F);
        final int second = Float.floatToIntBits(20F);
        final float[] array1 = ArrayUtils.toFloatArray(Bytes.concat(Ints.toByteArray(first), Ints.toByteArray(second)));
        assertArrayEquals(new float[]{10, 20}, array1, 0.001F);
        final Float[] array2 = ArrayUtils.toWrappedFloatArray(Bytes.concat(Ints.toByteArray(first), Ints.toByteArray(second)));
        assertArrayEquals(new Float[]{10F, 20F}, array2);
    }

    @Test
    public void toDoubleArrayTest(){
        final long first = Double.doubleToLongBits(10.0);
        final long second = Double.doubleToLongBits(20.0);
        final double[] array1 = ArrayUtils.toDoubleArray(Bytes.concat(Longs.toByteArray(first), Longs.toByteArray(second)));
        assertArrayEquals(new double[]{10.0, 20.0}, array1, 0.001F);
        final Double[] array2 = ArrayUtils.toWrappedDoubleArray(Bytes.concat(Longs.toByteArray(first), Longs.toByteArray(second)));
        assertArrayEquals(new Double[]{10.0, 20.0}, array2);
    }

    @Test
    public void toCharArrayTest(){
        final byte[] first = Chars.toByteArray('A');
        final byte[] second = Chars.toByteArray('Z');
        final char[] array1 = ArrayUtils.toCharArray(Bytes.concat(first, second));
        assertArrayEquals(new char[]{'A', 'Z'}, array1);
        final Character[] array2 = ArrayUtils.toWrappedCharArray(Bytes.concat(first, second));
        assertArrayEquals(new Character[]{'A', 'Z'}, array2);
    }

    @Test
    public void toBoolArrayTest(){
        final boolean[] array1 = ArrayUtils.toBoolArray(new byte[]{4});
        assertFalse(array1[0]);
        assertFalse(array1[1]);
        assertTrue(array1[2]);
        final Boolean[] array2 = ArrayUtils.toWrappedBoolArray(new byte[]{4});
        assertFalse(array2[0]);
        assertFalse(array2[1]);
        assertTrue(array2[2]);
    }

    @Test
    public void shortToByteArrayTest(){
        byte[] array1 = ArrayUtils.toByteArray(new short[]{10, 20});
        assertArrayEquals(Bytes.concat(Shorts.toByteArray((short) 10), Shorts.toByteArray((short) 20)), array1);
        array1 = ArrayUtils.toByteArray(new Short[]{10, 20});
        assertArrayEquals(Bytes.concat(Shorts.toByteArray((short) 10), Shorts.toByteArray((short) 20)), array1);
    }

    @Test
    public void intToArrayTest(){
        byte[] array1 = ArrayUtils.toByteArray(new int[]{10, 20});
        assertArrayEquals(Bytes.concat(Ints.toByteArray(10), Ints.toByteArray(20)), array1);
        array1 = ArrayUtils.toByteArray(new Integer[]{10, 20});
        assertArrayEquals(Bytes.concat(Ints.toByteArray(10), Ints.toByteArray(20)), array1);
    }

    @Test
    public void longToArrayTest(){
        byte[] array1 = ArrayUtils.toByteArray(new long[]{10L, 20L});
        assertArrayEquals(Bytes.concat(Longs.toByteArray(10L), Longs.toByteArray(20L)), array1);
        array1 = ArrayUtils.toByteArray(new Long[]{10L, 20L});
        assertArrayEquals(Bytes.concat(Longs.toByteArray(10L), Longs.toByteArray(20L)), array1);
    }

    @Test
    public void arrayConstructorTest(){
        final String[] arr = ArrayUtils.arrayConstructor(String.class).apply(10);
        assertEquals(10, arr.length);
    }

    @Test
    public void removeTest(){
        String[] arr = {"Danny"};
        arr = ArrayUtils.remove(arr, 0);
        assertEquals(0, arr.length);
    }

    @Test
    public void transformTest() {
        String[] result = ArrayUtils.transformByteArray(new byte[0], String.class, Integer::toString);
        assertEquals(0, result.length);
        result = ArrayUtils.transformByteArray(new byte[]{1, 2, 3}, String.class, Integer::toString);
        assertEquals(3, result.length);
        assertEquals("2", result[1]);
    }
}
