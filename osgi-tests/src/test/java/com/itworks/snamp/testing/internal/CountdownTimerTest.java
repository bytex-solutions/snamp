package com.itworks.snamp.testing.internal;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.CountdownTimer;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CountdownTimerTest extends AbstractUnitTest<CountdownTimer> {

    @Test
    public void timerTest1() throws InterruptedException {
        final CountdownTimer timer = new CountdownTimer(new TimeSpan(3000));
        assertTrue(timer.start());
        Thread.sleep(2000);
        assertTrue(timer.stop());
        assertTrue(timer.getElapsedTime().duration <= 1000);
        assertFalse(timer.isEmpty());
    }

    @Test
    public void timerTest2() throws InterruptedException{
        final CountdownTimer timer = new CountdownTimer(new TimeSpan(1000));
        assertTrue(timer.start());
        Thread.sleep(1000);
        assertTrue(timer.stop());
        assertTrue(timer.isEmpty());
    }
}
