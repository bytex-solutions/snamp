package com.snamp;

/**
 * Represents transactional (2PC) access to the thread-unsafe resource.
 * @param <R> Type of the thread-unsafe resource to hold.
 * @param <S> Type of the snapshot data that is used to restore the resource.
 * @param <C> Type of the changeset data that is used to commit changes into the resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class TransactionalResourceAccess<R, S, C> extends AbstractTransactionalResourceAccess<R, S, C> {
    private final R resource;

    /**
     * Initializes a new transaction coordinator for the specified thread unsafe resource.
     * @param resource A resource to wrap.
     */
    public TransactionalResourceAccess(final R resource){
        this.resource = resource;
    }

    /**
     * Returns the resource.
     *
     * @return The resource to synchronize.
     */
    @Override
    protected final R getResource() {
        return resource;
    }
}
