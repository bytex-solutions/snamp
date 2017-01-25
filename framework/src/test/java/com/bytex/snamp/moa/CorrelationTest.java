package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

/**
 * Represents test for {@link Correlation}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class CorrelationTest extends Assert {
    @Test
    public void simpleTest(){
        final Correlation cor = new Correlation();
        assertEquals(0D, cor.getAsDouble(), 0.01D);
        cor.applyAsDouble(10, 20);
        cor.applyAsDouble(5, 6);
        cor.applyAsDouble(3, 10);
        cor.applyAsDouble(90, 67);
        assertEquals(0.987, cor.getAsDouble(), 0.001D);
    }
}
