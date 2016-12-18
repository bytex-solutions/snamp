package com.bytex.snamp.core;

/**
 * Represents object which can be shared across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SharedObject {
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
