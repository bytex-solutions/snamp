package com.bytex.snamp.core;

import javax.annotation.Nonnull;

/**
 * Represents repository of shared objects.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public interface SharedObjectRepository<S extends SharedObject> {
    /**
     * Gets or creates shared object.
     * @param name Name of the shared object.
     * @return Shared object.
     */
    @Nonnull
    S getSharedObject(@Nonnull final String name);
    
    void releaseSharedObject(@Nonnull final String name);
}
