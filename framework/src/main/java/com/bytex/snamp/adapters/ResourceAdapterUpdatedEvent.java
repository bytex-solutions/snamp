package com.bytex.snamp.adapters;

/**
 * Represents an event indicating that the updating of managed resource adapter instance
 * is completed.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceAdapterUpdatedEvent extends ResourceAdapterEvent {
    private static final long serialVersionUID = -515503467706353840L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    public ResourceAdapterUpdatedEvent(final ResourceAdapter adapterInstance) {
        super(adapterInstance);
    }
}
