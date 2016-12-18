package com.bytex.snamp.core;

/**
 * Represents distributed service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface DistributedService {
    /**
     * Gets name of the distributed service.
     * @return Name of this distributed service.
     */
    String getName();

    /**
     * Determines whether this service is backed by persistent storage.
     * @return {@literal true}, if this service is backed by persistent storage; otherwise, {@literal false}.
     */
    boolean isPersistent();
}
