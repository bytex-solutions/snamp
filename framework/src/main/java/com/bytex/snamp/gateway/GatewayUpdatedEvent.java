package com.bytex.snamp.gateway;

/**
 * Represents an event indicating that the updating of gateway instance
 * is completed.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class GatewayUpdatedEvent extends GatewayEvent {
    private static final long serialVersionUID = -515503467706353840L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param gatewayInstance An instance of the gateway associated with this event.
     */
    GatewayUpdatedEvent(final Gateway gatewayInstance) {
        super(gatewayInstance);
    }
}
