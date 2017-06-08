package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

import java.math.MathContext;
import java.time.temporal.ChronoUnit;

/**
 * Represents tests for {@link DoubleEMA}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class EMATest extends Assert {
    @Test
    public void doubleEmaTest() throws InterruptedException {
        final AbstractEMA ema = new DoubleEMA(2, ChronoUnit.SECONDS);
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
        final AbstractEMA ema = new BigDecimalEMA(2, ChronoUnit.SECONDS, MathContext.DECIMAL32);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);
    }
}
