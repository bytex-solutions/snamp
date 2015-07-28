package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ArrayEnumeratorTest extends Assert {
    @Test
    public void enumerationTest(){
        final Integer[] array = {4, 8, 3};
        final ArrayEnumerator<Integer> enu = new ArrayEnumerator<>(array);
        assertTrue(enu.hasMoreElements());
        assertEquals(4, (int) enu.nextElement());
        assertEquals(8, (int)enu.nextElement());
        assertEquals(3, (int)enu.nextElement());
        try{
            enu.nextElement();
        }
        catch (final NoSuchElementException ignored){
            return;
        }
        fail("Unexpected statement");
    }

    @Test
    public void loopTest(){
        final Integer[] array = {4, 8, 3};
        final ArrayEnumerator<Integer> enu = new ArrayEnumerator<>(array);
        while (enu.hasMoreElements())
            enu.nextElement();
    }
}
