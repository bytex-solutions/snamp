package com.bytex.snamp;

import com.bytex.snamp.jmx.CompositeTypeBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.*;
import java.lang.reflect.Array;

/**
 * @author Roman Sakno
 * @version 1.0
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
    public void emptyOpenArrayTest() throws OpenDataException {
        byte[] array1 = ArrayUtils.emptyArray(new ArrayType<byte[]>(SimpleType.BYTE, true), getClass().getClassLoader());
        assertTrue(array1.length == 0);
        Byte[] array2 = ArrayUtils.emptyArray(new ArrayType<Byte[]>(SimpleType.BYTE, false), getClass().getClassLoader());
        assertTrue(array2.length == 0);
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
    public void openTypeArrayTest() throws OpenDataException {
        Object array = ArrayUtils.newArray(new ArrayType<Boolean[]>(SimpleType.BOOLEAN, true), 10);
        assertTrue(array instanceof boolean[]);
        assertEquals(10, Array.getLength(array));
        array = ArrayUtils.newArray(new ArrayType<Boolean[]>(SimpleType.BOOLEAN, false), 11);
        assertTrue(array instanceof Boolean[]);
        assertEquals(11, Array.getLength(array));
        array = ArrayUtils.newArray(new ArrayType<Boolean[]>(SimpleType.STRING, false), 5);
        assertTrue(array instanceof String[]);
        assertEquals(5, Array.getLength(array));
        final CompositeType ct = new CompositeTypeBuilder("dummyType", "dummy")
                .addItem("x", "X coordinate", SimpleType.LONG)
                .addItem("y", "Y coordinate", SimpleType.LONG)
                .build();
        array = ArrayUtils.newArray(new ArrayType<CompositeData[]>(1, ct), 7);
        assertTrue(array instanceof CompositeData[]);
        assertEquals(7, Array.getLength(array));
    }

    @Test
    public void boxArrayTest(){
        final Byte[] result = ArrayUtils.boxArray(new byte[]{2, 5, 7});
        assertEquals(3, result.length);
        assertEquals(new Byte((byte)2), result[0]);
        assertEquals(new Byte((byte)5), result[1]);
        assertEquals(new Byte((byte)7), result[2]);
    }

    @Test
    public void unboxArrayTest(){
        final int[] result = ArrayUtils.unboxArray(new Integer[]{2, 5, 7});
        assertEquals(3, result.length);
        assertEquals(2, result[0]);
        assertEquals(5, result[1]);
        assertEquals(7, result[2]);
    }
}
