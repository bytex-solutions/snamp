package com.bytex.snamp.concurrent;

/**
 * Provides basic support for synchronization as separation-of-concerns pattern.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Awaitable<T, E extends Throwable> {
    /**
     * Gets awaitor for this asynchronous object.
     * @return Awaitor for this object.
     */
    Awaitor<T, E> getAwaitor();
}
