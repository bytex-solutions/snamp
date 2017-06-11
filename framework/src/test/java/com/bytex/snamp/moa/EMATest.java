package com.bytex.snamp.moa;

import org.junit.Assert;
import org.junit.Test;

import java.math.MathContext;
import java.time.Duration;

/**
 * Represents tests for {@link DoubleEWMA}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class EMATest extends Assert {
    @Test
    public void doubleEmaTest() throws InterruptedException {
        final EWMA ema = new DoubleEWMA(Duration.ofSeconds(2));
        ema.append(10);
        ema.append(20);
        Thread.sleep(2001);
        ema.append(10);
        ema.append(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);

        ema.reset();
        ema.accept(10);
        ema.accept(30);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(30);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);
    }

    @Test
    public void bigDecimalEmaTest() throws InterruptedException{
        final EWMA ema = new BigDecimalEWMA(Duration.ofSeconds(2), MathContext.DECIMAL32);
        ema.append(10);
        ema.append(20);
        Thread.sleep(2001);
        ema.append(10);
        ema.append(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);

        ema.reset();
        ema.accept(10);
        ema.accept(30);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(30);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.doubleValue(), 0.1D);
    }
}
