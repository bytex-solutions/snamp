package com.bytex.snamp.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.OptionalInt;

/**
 * Represents test for {@link DoubleReservoir}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DoubleReservoirTest extends Assert {
    @Test
    public void sizeLimitTest(){
        final DoubleReservoir reservoir = new DoubleReservoir(3);
        reservoir.add(5D);
        reservoir.add(6D);
        reservoir.add(10D);
        reservoir.add(3D);
        reservoir.add(1D);
        reservoir.add(15D);
        assertEquals(1D, reservoir.get(0), 0.1D);
        assertEquals(6D, reservoir.get(1), 0.1D);
        assertEquals(15D, reservoir.get(2), 0.1D);
    }

    @Test
    public void simpleTest(){
        final DoubleReservoir reservoir = new DoubleReservoir(15);
        reservoir.add(10);
        reservoir.add(20);
        reservoir.add(30);
        reservoir.add(5);
        reservoir.add(3);
        reservoir.add(15);
        reservoir.add(18);
        reservoir.add(19);
        reservoir.add(32);
        reservoir.add(26);
        assertEquals(10, reservoir.getSize());
        assertEquals(15, reservoir.getCapacity());
        assertEquals(17.8D, reservoir.getMean(), 0.01D);
        assertEquals(24.2D, reservoir.getQuantile(0.7), 0.1D);
    }

    @Test
    public void findTest(){
        final DoubleReservoir reservoir = new DoubleReservoir(5);
        reservoir.add(5D);//index 2
        reservoir.add(3D);
        reservoir.add(1D);//index 0
        reservoir.add(9D);
        final OptionalInt searchResult = reservoir.find(5D);
        assertTrue(searchResult.isPresent());
        assertEquals(2, searchResult.getAsInt());
        assertEquals(0.25D, reservoir.greaterThanOrEqualValues(6D), 0.001D);
        assertEquals(0.75D, reservoir.lessThanOrEqualValues(5D), 0.001D);
    }
}
