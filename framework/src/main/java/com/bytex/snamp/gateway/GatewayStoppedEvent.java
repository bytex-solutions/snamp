package com.bytex.snamp.gateway;

/**
 * Represents an event indicating that the gateway instance is stopped.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class GatewayStoppedEvent extends GatewayEvent {
    private static final long serialVersionUID = -3782820204672801670L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param gatewayInstance An instance of the gateway associated with this event.
     */
    public GatewayStoppedEvent(final Gateway gatewayInstance) {
        super(gatewayInstance);
    }
}
