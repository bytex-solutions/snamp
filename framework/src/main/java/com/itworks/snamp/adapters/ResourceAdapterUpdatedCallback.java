package com.itworks.snamp.adapters;

/**
 * The callback invoked when updating of resource adapter is completed.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapterUpdatedCallback {
    /**
     * Represents the callback that does nothing.
     */
    ResourceAdapterUpdatedCallback STUB = new ResourceAdapterUpdatedCallback() {
        @Override
        public void updated() {

        }
    };

    /**
     * Updating of the resource adapter is completed.
     */
    void updated();
}
