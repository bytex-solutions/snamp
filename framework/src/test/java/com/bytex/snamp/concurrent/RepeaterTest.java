package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class RepeaterTest extends Assert {
    private static final class SecondsCounter extends Repeater{
        private final AtomicLong c = new AtomicLong(0);

        private SecondsCounter(){
            super(Duration.ofSeconds(1));
        }

        public long getValue(){
            return c.get();
        }

        /**
         * Handles a new repeater state.
         * <p>
         * In the default implementation this method does nothing.
         * You can override this method to handle the repeater state.
         * </p>
         *
         * @param s A new repeater state.
         */
        @Override
        protected void stateChanged(final RepeaterState s) {
            switch (s){
                case STARTED: c.getAndSet(0L);
            }
        }

        /**
         * Provides some periodical action.
         */
        @Override
        protected void doAction() {
            c.incrementAndGet();
        }
    }

    @Test
    public final void occurencesTest() throws InterruptedException, TimeoutException {
        try(final SecondsCounter counter = new SecondsCounter()){
            counter.run();
            Thread.sleep(5400);
            counter.stop(Duration.ofMillis(400));
            assertEquals(5, counter.getValue());
            assertEquals(Repeater.RepeaterState.STOPPED, counter.getState());
        }
    }

    @Test
    public final void exceptionTest() throws InterruptedException, TimeoutException {
        final String exceptionMessage = "Test exception";
        try(final Repeater rep = new Repeater(Duration.ofMillis(1)) {
            private final AtomicLong counter = new AtomicLong(0);

            @Override
            protected void doAction() {
                if(counter.incrementAndGet() == 3) try {
                    throw new Exception(exceptionMessage);
                } catch (final Exception e) {
                    fault(e);
                }
            }
        }){
            rep.run();
            Thread.sleep(4000);
            assertEquals(Repeater.RepeaterState.FAILED, rep.getState());
            assertNotNull(rep.getException());
            assertEquals(exceptionMessage, rep.getException().getMessage());
            assertEquals(Repeater.RepeaterState.FAILED, rep.getState());
        }
    }
}
