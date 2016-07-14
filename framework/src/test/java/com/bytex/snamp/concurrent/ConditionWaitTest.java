package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ConditionWaitTest extends Assert {
    @Test
    public void simpleTest() throws Exception{
        final long EXPECTED_VALUE = 10L;
        final AtomicLong counter = new AtomicLong(0L);
        final ConditionWait condition = ConditionWait.create(counter, counter1 -> counter1.getAndIncrement() >= EXPECTED_VALUE);
        final Object result;
        assertNotNull(result = condition.get(5, TimeUnit.MINUTES));
        assertEquals(result, condition.get());
        assertEquals(EXPECTED_VALUE + 1, counter.get());
    }
}
