package com.bytex.snamp.gateway;

/**
 * Represents state of the resource adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum GatewayState {
    /**
     * Adapter is created but not started.
     */
    CREATED,

    /**
     * Adapter is started.
     */
    STARTED,

    /**
     * Adapter is stopped.
     */
    STOPPED,

    /**
     * Adapter is closed.
     */
    CLOSED
}
