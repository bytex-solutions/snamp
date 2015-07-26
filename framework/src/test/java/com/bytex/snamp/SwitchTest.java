package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SwitchTest extends Assert {

    @Test
    public void simpleTest(){
        @SuppressWarnings("ConstantConditions")
        final boolean result = new Switch<Long, Boolean>()
                .equals(42L, Boolean.FALSE)
                .equals(43L, Boolean.TRUE)
                .apply(43L);
        assertTrue(result);
    }
}
