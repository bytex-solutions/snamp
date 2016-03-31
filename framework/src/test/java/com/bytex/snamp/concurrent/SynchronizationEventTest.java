package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SynchronizationEventTest extends Assert {

    @Test
    public final void manualResetTest() throws InterruptedException, ExecutionException {
        final SynchronizationEvent<String> event = new SynchronizationEvent<>();
        final Future<String> awaitor1 = event.getAwaitor();
        assertTrue(event.fire("Signal #1"));
        assertEquals("Signal #1", awaitor1.get());
        final Future<String> awaitor2 = event.getAwaitor();
        assertFalse(event.fire("Signal #2"));
        assertEquals(awaitor1, awaitor2);
        assertEquals("Signal #1", awaitor2.get());
    }

    @Test
    public final void autoResetTest() throws InterruptedException, ExecutionException {
        final SynchronizationEvent<String> event = new SynchronizationEvent<>(true);
        final Future<String> awaitor1 = event.getAwaitor();
        assertTrue(event.fire("Signal #1"));
        assertEquals("Signal #1", awaitor1.get());
        final Future<String> awaitor2 = event.getAwaitor();
        assertTrue(event.fire("Signal #2"));
        assertEquals("Signal #2", awaitor2.get());
        assertEquals("Signal #1", awaitor1.get());
        assertNotEquals(awaitor1, awaitor2);
    }
}
