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
        final EWMA ema = DoubleEWMA.floatingInterval(Duration.ofMillis(300));
        Thread.sleep(100);
        ema.accept(3);

        Thread.sleep(100);
        ema.accept(3);

        Thread.sleep(100);
        ema.accept(2);

        assertEquals(2.7D, ema.doubleValue(), 0.1D);
    }

    @Test
    public void bigDecimalEmaTest() throws InterruptedException{
        final EWMA ema = BigDecimalEWMA.floatingInterval(Duration.ofMillis(300), MathContext.DECIMAL64);
        Thread.sleep(100);
        ema.accept(3);

        Thread.sleep(100);
        ema.accept(3);

        Thread.sleep(100);
        ema.accept(2);

        assertEquals(2.7D, ema.doubleValue(), 0.1D);
    }
}
