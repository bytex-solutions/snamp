package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

import java.math.MathContext;
import java.time.temporal.ChronoUnit;

/**
 * Represents tests for {@link DoubleEWMA}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class EMATest extends Assert {
    @Test
    public void doubleEmaTest() throws InterruptedException {
        final EWMA ema = new DoubleEWMA(2, ChronoUnit.SECONDS);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);
    }

    @Test
    public void bigDecimalEmaTest() throws InterruptedException{
        final EWMA ema = new BigDecimalEWMA(2, ChronoUnit.SECONDS, MathContext.DECIMAL32);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);
    }
}
