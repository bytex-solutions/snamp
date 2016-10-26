package com.bytex.snamp.gateway.http;

import org.atmosphere.cpr.Broadcaster;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents HTTP Gateway-specific broadcaster.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface InternalBroadcaster extends Broadcaster {

    /**
     * Initializes broadcaster using the request.
     * @param request The subscription request.
     */
    void initialize(final HttpServletRequest request) throws Exception;
}
