package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.groovy.GatewayScript;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents simple container for {@link GatewayScript} object.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ScriptHolder extends AtomicReference<GatewayScript> implements AutoCloseable, NotificationListener {
    private static final long serialVersionUID = 1639929721374659443L;

    @Override
    public void close() throws Exception {
        final GatewayScript script = getAndSet(null);
        if(script != null) script.close();
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public void handleNotification(final NotificationEvent event) {
        final GatewayScript script = get();
        if(script != null) script.handleNotification(event);
    }
}
