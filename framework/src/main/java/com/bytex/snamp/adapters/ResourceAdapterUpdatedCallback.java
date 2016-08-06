package com.bytex.snamp.adapters;

/**
 * The callback invoked when updating of resource adapter is completed.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
public interface ResourceAdapterUpdatedCallback {

    /**
     * Updating of the resource adapter is completed.
     */
    void updated();
}
