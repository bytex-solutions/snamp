package com.bytex.snamp.gateway;

/**
 * Represents an event indicating that the managed resource adapter instance
 * is in updating state.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class GatewayUpdatingEvent extends GatewayEvent {
    private static final long serialVersionUID = -2452447516796041643L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param gatewayInstance An instance of the resource adapter associated with this event.
     */
    public GatewayUpdatingEvent(final Gateway gatewayInstance) {
        super(gatewayInstance);
    }
}
