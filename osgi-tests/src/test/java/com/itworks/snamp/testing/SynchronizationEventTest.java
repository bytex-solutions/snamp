package com.itworks.snamp.testing;

import com.itworks.snamp.SynchronizationEvent;
import org.junit.Test;
import static com.itworks.snamp.SynchronizationEvent.Awaitor;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SynchronizationEventTest extends AbstractUnitTest<SynchronizationEvent> {
    public SynchronizationEventTest() {
        super(SynchronizationEvent.class);
    }

    @Test
    public final void manualResetTest() throws InterruptedException{
        final SynchronizationEvent<String> event = new SynchronizationEvent<>();
        final Awaitor<String> awaitor1 = event.getAwaitor();
        assertTrue(event.fire("Signal #1"));
        assertEquals("Signal #1", awaitor1.await());
        final Awaitor<String> awaitor2 = event.getAwaitor();
        assertFalse(event.fire("Signal #2"));
        assertEquals(awaitor1, awaitor2);
        assertEquals("Signal #1", awaitor2.await());
    }

    @Test
    public final void autoResetTest() throws InterruptedException {
        final SynchronizationEvent<String> event = new SynchronizationEvent<>(true);
        final Awaitor<String> awaitor1 = event.getAwaitor();
        assertTrue(event.fire("Signal #1"));
        assertEquals("Signal #1", awaitor1.await());
        final Awaitor<String> awaitor2 = event.getAwaitor();
        assertTrue(event.fire("Signal #2"));
        assertEquals("Signal #2", awaitor2.await());
        assertEquals("Signal #1", awaitor1.await());
        assertNotEquals(awaitor1, awaitor2);
    }
}
