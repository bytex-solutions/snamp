package com.itworks.snamp.concurrent;

import com.itworks.snamp.TimeSpan;

import java.util.concurrent.TimeoutException;

/**
 * Represents awaitor of the synchronization event.
 * <p>
 * This interface should be used by event consumer.
 * </p>
 *
 * @param <T> Type of the event result.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Awaitor<T, E extends Throwable> {
    /**
     * Blocks the caller thread until the event will not be raised.
     *
     * @param timeout Event waiting timeout.
     * @return The event data.
     * @throws java.util.concurrent.TimeoutException     timeout parameter too small for waiting.
     * @throws InterruptedException Waiting thread is aborted.
     */
    T await(final TimeSpan timeout) throws TimeoutException, InterruptedException, E;

    /**
     * Blocks the caller thread (may be infinitely) until the event will not be raised.
     *
     * @return The event data.
     * @throws InterruptedException Waiting thread is aborted.
     */
    T await() throws InterruptedException, E;
}
