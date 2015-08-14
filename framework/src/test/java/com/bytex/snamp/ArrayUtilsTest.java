package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

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
}
