package com.bytex.snamp.gateway;

import java.util.EventListener;

/**
 * The callback invoked when updating of gateway instance is completed.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
public interface GatewayUpdatedCallback extends EventListener {

    /**
     * Updating of the gateway instance is completed.
     */
    void updated();
}
