package com.bytex.snamp.adapters.http;

import org.atmosphere.cpr.Broadcaster;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents HTTP Adapter-specific broadcaster.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface InternalBroadcaster extends Broadcaster {

    /**
     * Initializes broadcaster using the request.
     * @param request The subscription request.
     */
    void initialize(final HttpServletRequest request) throws Exception;
}
