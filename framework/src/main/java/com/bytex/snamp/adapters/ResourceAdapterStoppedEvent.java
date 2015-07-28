package com.bytex.snamp.adapters;

/**
 * Represents an event indicating that the managed resource adapter instance
 * is stopped.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceAdapterStoppedEvent extends ResourceAdapterEvent {
    private static final long serialVersionUID = -3782820204672801670L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    public ResourceAdapterStoppedEvent(final ResourceAdapter adapterInstance) {
        super(adapterInstance);
    }
}
