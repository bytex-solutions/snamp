package com.bytex.snamp.adapters;

/**
 * The callback invoked when updating of resource adapter is completed.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ResourceAdapterUpdatedCallback {
    /**
     * Represents the callback that does nothing.
     * @deprecated Use empty lambda instead.
     */
    @Deprecated
    ResourceAdapterUpdatedCallback STUB = () -> {};

    /**
     * Updating of the resource adapter is completed.
     */
    void updated();
}
