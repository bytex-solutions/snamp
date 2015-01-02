package com.itworks.snamp.views;

import java.util.concurrent.ExecutorService;

/**
 * Represents an object that can produce a view of itself which supports
 * parallel execution of some methods.
 * @param <T> Type of the object implementing {@link ParallelView}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ParallelView<T extends ParallelView<T>> extends View {
    /**
     * Returns an equivalent object that is parallel.
     * May return itself, either because the object was already parallel,
     * or because the underlying object state was modified to be parallel.
     * @param executor An executor used to apply methods in parallel manner.
     * @return An object that supports parallel execution of some methods.
     */
    T parallel(final ExecutorService executor);
}
