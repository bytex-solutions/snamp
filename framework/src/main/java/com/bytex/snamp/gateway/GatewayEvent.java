package com.bytex.snamp.gateway;

import java.util.EventObject;

/**
 * The root class from which all gateway-related event state objects shall be derived.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class GatewayEvent extends EventObject {
    private static final long serialVersionUID = -7833423864797063691L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param gatewayInstance An instance of the gateway associated with this event.
     */
    GatewayEvent(final Gateway gatewayInstance) {
        super(gatewayInstance);
    }

    /**
     * Gets the gateway instance associated with this event.
     *
     * @return The gateway instance associated with this event.
     */
    @Override
    public final Gateway getSource() {
        return (Gateway) source;
    }
}
