package com.itworks.snamp.testing;

import com.itworks.snamp.Switch;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SwitchTest extends AbstractUnitTest<Switch> {

    @Test
    public void simpleTest(){
        final Boolean result = new Switch<Long, Boolean>()
                .equals(42L, Boolean.FALSE)
                .equals(43L, Boolean.TRUE)
                .apply(43L);
        assertTrue(result);
    }
}
