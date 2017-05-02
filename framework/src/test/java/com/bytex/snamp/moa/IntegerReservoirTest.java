package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

import java.util.OptionalInt;

/**
 * Provides tests for {@link IntegerReservoir}.
 */
public final class IntegerReservoirTest extends Assert {
    @Test
    public void sizeLimitTest() {
        final IntegerReservoir reservoir = new IntegerReservoir(3);
        reservoir.add(5);
        reservoir.add(6);
        reservoir.add(10);
        reservoir.add(3);
        reservoir.add(1);
        reservoir.add(15);
        assertEquals(1, reservoir.get(0));
        assertEquals(6, reservoir.get(1));
        assertEquals(15, reservoir.get(2));
        assertEquals(1, reservoir.getMin().orElseThrow(AssertionError::new));
        assertEquals(15, reservoir.getMax().orElseThrow(AssertionError::new));
    }

    @Test
    public void simpleTest(){
        final IntegerReservoir reservoir = new IntegerReservoir(15);
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
        assertEquals(24.2D, reservoir.getQuantile(0.7F), 0.1D);
    }

    @Test
    public void findTest(){
        final IntegerReservoir reservoir = new IntegerReservoir(5);
        reservoir.add(5);//index 2
        reservoir.add(3);
        reservoir.add(1);//index 0
        reservoir.add(9);
        final OptionalInt searchResult = reservoir.find(5);
        assertTrue(searchResult.isPresent());
        assertEquals(2, searchResult.getAsInt());
        assertEquals(0.25D, reservoir.greaterThanOrEqualValues(6), 0.001D);
        assertEquals(0.75D, reservoir.lessThanOrEqualValues(5), 0.001D);
    }

    @Test
    public void sumTest(){
        final IntegerReservoir reservoir = new IntegerReservoir(5);
        reservoir.add(5);
        reservoir.add(3);
        reservoir.add(1);
        reservoir.add(9);
        assertEquals(18, reservoir.sum());
    }
}
