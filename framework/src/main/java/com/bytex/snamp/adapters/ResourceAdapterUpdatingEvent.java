package com.bytex.snamp.adapters;

/**
 * Represents an event indicating that the managed resource adapter instance
 * is in updating state.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceAdapterUpdatingEvent extends ResourceAdapterEvent {
    private static final long serialVersionUID = -2452447516796041643L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    public ResourceAdapterUpdatingEvent(final ResourceAdapter adapterInstance) {
        super(adapterInstance);
    }
}
