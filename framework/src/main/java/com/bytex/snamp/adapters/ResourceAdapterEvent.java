package com.bytex.snamp.adapters;

import java.util.EventObject;

/**
 * The root class from which all adapter-related event state objects shall be derived.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceAdapterEvent extends EventObject {
    private static final long serialVersionUID = -7833423864797063691L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    ResourceAdapterEvent(final ResourceAdapter adapterInstance) {
        super(adapterInstance);
    }

    /**
     * Gets the resource adapter associated with this event.
     *
     * @return The resource adapter associated with this event.
     */
    @Override
    public final ResourceAdapter getSource() {
        return (ResourceAdapter) source;
    }
}
