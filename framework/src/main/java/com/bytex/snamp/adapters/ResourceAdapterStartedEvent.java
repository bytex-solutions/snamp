package com.bytex.snamp.adapters;

/**
 * Represents an event indicating that the managed resource adapter instance
 * is started.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ResourceAdapterStartedEvent extends ResourceAdapterEvent {
    private static final long serialVersionUID = -1707839465749376975L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    public ResourceAdapterStartedEvent(final ResourceAdapter adapterInstance) {
        super(adapterInstance);
    }
}
