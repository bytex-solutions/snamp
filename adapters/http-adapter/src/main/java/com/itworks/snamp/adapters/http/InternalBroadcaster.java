package com.itworks.snamp.adapters.http;

import org.atmosphere.cpr.Broadcaster;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents HTTP Adapter-specific broadcaster.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface InternalBroadcaster extends Broadcaster {
    /**
     * Represents broadcaster initialization state.
     */
    static enum BroadcasterState {
        /**
         * Indicates that the broadcaster is initialized successfully.
         */
        INITIALIZED,
        /**
         * Indicates that the broadcaster could not be initialized.
         */
        NOT_INITIALIZED,
        /**
         * Indicates that the broadcaster was been initialized.
         */
        ALREADY_INITIALIZED
    }

    /**
     * Initializes broadcaster using the request.
     * @param request The subscription request.
     * @return Broadcaster state.
     */
    BroadcasterState init(final HttpServletRequest request);
}
