package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Represents tests for {@link Timeout}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TimeoutTest extends Assert {
    @Test
    public void timeMeasurementTest() throws InterruptedException {
        final Timeout t = new Timeout(100, TimeUnit.MILLISECONDS);
        assertFalse(t.isExpired());
        Thread.sleep(30);
        assertFalse(t.isExpired());
        assertFalse(t.runIfExpired(() -> {}));
        Thread.sleep(100);
        assertTrue(t.runIfExpired(() -> {}));
        assertFalse(t.isExpired());
    }
}
