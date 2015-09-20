package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

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
}
