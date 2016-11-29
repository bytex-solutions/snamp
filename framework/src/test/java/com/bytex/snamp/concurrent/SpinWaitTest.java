package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bytex.snamp.concurrent.SpinWait.spinUntil;
import static com.bytex.snamp.concurrent.SpinWait.spinUntilNull;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SpinWaitTest extends Assert {
    @Test
    public void spinUntilTest() throws TimeoutException, InterruptedException {
        final AtomicInteger counter = new AtomicInteger(-100);
        spinUntil(() -> counter.incrementAndGet() < 0, Duration.ofSeconds(1));
        assertEquals(0, counter.get());
    }

    @Test
    public void spinUntilNullTest() throws Exception {
        final AtomicInteger counter = new AtomicInteger(-100);
        final Object result = spinUntilNull(() -> counter.incrementAndGet() == 0 ? new Object() : null, Duration.ofSeconds(1));
        assertNotNull(result);
        assertEquals(0, counter.get());
    }

    @Test
    public void spinUntilNullTest2() throws Exception {
        final AtomicInteger counter = new AtomicInteger(-100);
        final Object result = spinUntilNull(counter, c -> c.incrementAndGet() == 0 ? new Object() : null, Duration.ofSeconds(1));
        assertNotNull(result);
        assertEquals(0, counter.get());
    }

    @Test
    public void spinUntilNullTest3() throws Exception {
        final AtomicInteger counter = new AtomicInteger(-100);
        final Object result = spinUntilNull(counter, new Object(), (c, r) -> c.incrementAndGet() == 0 ? r : null, Duration.ofSeconds(1));
        assertNotNull(result);
        assertEquals(0, counter.get());
    }
}
