package com.bytex.snamp.gateway;

import java.util.EventListener;

/**
 * Represents listener for events related to gateway instance.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 * @see GatewayClient#addEventListener(String, GatewayEventListener)
 * @see GatewayClient#removeEventListener(String, GatewayEventListener)
 */
public interface GatewayEventListener extends EventListener {
    /**
     * Invokes after gateway instance started.
     * @param e An event object that describes the started gateway instance.
     */
    void handle(final GatewayEvent e);
}
