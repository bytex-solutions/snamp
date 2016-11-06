package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConvertTest extends Assert {
    @Test
    public void toLongConversion(){
        assertEquals(10, Convert.toLong("10"));
        assertEquals(20, Convert.toLong(20.12D));
    }
}
