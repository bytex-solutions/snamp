package com.bytex.snamp.gateway;

/**
 * Represents an event indicating that the managed resource adapter instance
 * is started.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class GatewayStartedEvent extends GatewayEvent {
    private static final long serialVersionUID = -1707839465749376975L;

    /**
     * Initializes a new instance of the event data holder.
     *
     * @param adapterInstance An instance of the resource adapter associated with this event.
     */
    public GatewayStartedEvent(final Gateway adapterInstance) {
        super(adapterInstance);
    }
}
