package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class QTest extends Assert {
    @Test
    public void factorialTest(){
        assertEquals(6D, Q.factorial(3), 0.01D);
        assertEquals(1D, Q.factorial(0), 0.01D);
        assertTrue(Double.isInfinite(Q.factorial(200)));
        assertEquals(1D, Q.factorial(1), 0.01D);
        assertEquals(6227020800D, Q.factorial(13), 0.01D);
    }
}
