package com.bytex.snamp.core;

/**
 * Represents object which can be shared across cluster nodes.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface SharedObject {
    /**
     * Gets name of the distributed service.
     * @return Name of this distributed service.
     */
    String getName();
}
