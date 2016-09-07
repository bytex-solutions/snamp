package com.bytex.snamp.math;

import org.junit.Test;

/**
 * Represents test for {@link DoubleReservoir}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DoubleReservoirTest {
    @Test
    public void shiftTest(){
        final DoubleReservoir q = new DoubleReservoir(5);
        q.add(10);
        q.add(20);
        q.add(30);
        q.add(5);
    }
}
