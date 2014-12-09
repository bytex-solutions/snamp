package com.itworks.snamp.views;

/**
 * Represents an object that can produce a view of itself with support
 * sequential execution of some methods.
 * @param <T> Type of the object implementing {@link SequentialViewSupport}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SequentialViewSupport<T extends SequentialViewSupport<T>> extends View {
    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     * @return An object that supports sequential execution of some methods.
     */
    T sequential();
}
