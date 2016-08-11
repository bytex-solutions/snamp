package com.bytex.snamp.gateway;

/**
 * Represents state of the gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum GatewayState {
    /**
     * Gateway instance is created but not started.
     */
    CREATED,

    /**
     * Gateway instance is started.
     */
    STARTED,

    /**
     * Gateway instance is stopped but can be started again.
     */
    STOPPED,

    /**
     * Gateway is closed and cannot be started again.
     */
    CLOSED
}
