package com.snamp;

import java.lang.ref.WeakReference;

/**
 * Represents transactional access to the resource using weak reference to the wrapped
 * resource.
 * @param <R> Type of the thread-unsafe resource to hold.
 * @param <S> Type of the snapshot data that is used to restore the resource.
 * @param <C> Type of the changeset data that is used to commit changes into the resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class TransactionalWeakResourceAccess<R, S, C> extends AbstractTransactionalResourceAccess<R, S, C> {
    private final WeakReference<R> resource;

    /**
     * Initializes a new transaction coordinator for the specified resource.
     * <p>
     *     The constructor obtains only weak reference to the specified resource.
     * </p>
     * @param resource The resource to coordinate.
     */
    public TransactionalWeakResourceAccess(final R resource){
        this.resource = new WeakReference<>(resource);
    }

    /**
     * Returns the resource.
     *
     * @return The resource to synchronize.
     */
    @Override
    protected final R getResource() {
        return resource.get();
    }
}
