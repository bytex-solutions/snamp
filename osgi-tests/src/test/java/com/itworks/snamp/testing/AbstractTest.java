package com.itworks.snamp.testing;

import org.junit.Assert;

import java.lang.reflect.Array;

/**
 * Represents a base class for all SNAMP-specific tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTest extends Assert {


    @SafeVarargs
    protected static <T> T[] concat(final T[] array1, final T... array2){
        @SuppressWarnings("unchecked")
        final T[] joinedArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    protected static void assertArrayEquals(final Object expected, final Object actual){
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for(int i = 0; i < Array.getLength(expected); i++)
            assertEquals(Array.get(expected, i), Array.get(actual, i));
    }
}
