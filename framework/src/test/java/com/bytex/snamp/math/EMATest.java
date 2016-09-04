package com.bytex.snamp.math;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Represents tests for {@link ExponentiallyMovingAverage}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class EMATest extends Assert {
    @Test
    public void basicTest() throws InterruptedException {
        final ExponentiallyMovingAverage ema = new ExponentiallyMovingAverage(Duration.ofSeconds(2));
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        ema.accept(10);
        ema.accept(20);
        Thread.sleep(2001);
        assertEquals(13.3D, ema.getAsDouble(), 0.1D);
    }
}
