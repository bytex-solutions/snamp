package com.snamp;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents synchronization event that is used to synchronize with some
 * asynchronous event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SynchronizationEvent<T> {
    private CountDownLatch barrier;
    private boolean raised;
    private T eventObj;

    /**
     * Represents awaitor of the synchronization event.
     * <p>
     *     This interface should be used by event consumer.
     * </p>
     * @param <T> Type of the event result.
     */
    public static interface Awaitor<T>{
        /**
         * Blocks the caller thread until the event will not be raised.
         * @param timeout Event waiting timeout.
         * @return The event data.
         * @throws TimeoutException timeout parameter too small for waiting.
         * @throws InterruptedException Waiting thread is aborted.
         */
        public T await(final TimeSpan timeout) throws TimeoutException, InterruptedException;

        /**
         * Blocks the caller thread (may be infinitely) until the event will not be raised.
         * @return The event data.
         * @throws InterruptedException Waiting thread is aborted.
         */
        public T await() throws InterruptedException;
    }

    /**
     * Initializes a new synchronization event.
     */
    public SynchronizationEvent(){
        barrier = new CountDownLatch(1);
        raised = false;
        eventObj = null;
    }

    /**
     * Resets state of this event to initial.
     */
    public synchronized final void reset(){
        this.barrier = new CountDownLatch(1);
        this.raised = false;
        this.eventObj = null;
    }

    /**
     * Fires the event.
     * @param eventObj The raised event data.
     */
    public synchronized final void fire(final T eventObj){
        if(raised) return;
        this.raised = true;
        this.eventObj = eventObj;
        this.barrier.countDown();
    }

    /**
     * Creates a new awaitor for this event.
     * @return A new awaitor for this event.
     */
    public final Awaitor<T> getAwaitor(){
        return new Awaitor<T>() {
            @Override
            public final T await(final TimeSpan timeout) throws TimeoutException, InterruptedException {
                if(!barrier.await(timeout.duration, timeout.unit)) throw new TimeoutException();
                else return eventObj;
            }

            @Override
            public final T await() throws InterruptedException {
                barrier.await();
                return eventObj;
            }
        };
    }
}
