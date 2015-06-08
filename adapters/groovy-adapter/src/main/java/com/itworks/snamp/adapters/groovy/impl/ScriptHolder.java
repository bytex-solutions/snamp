package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.adapters.NotificationEvent;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.groovy.ResourceAdapterScript;
import com.itworks.snamp.concurrent.VolatileBox;

/**
 * Represents simple container for {@link ResourceAdapterScript} object.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptHolder extends VolatileBox<ResourceAdapterScript> implements AutoCloseable, NotificationListener {

    @Override
    public void close() throws Exception {
        final ResourceAdapterScript script = getAndSet(null);
        if(script != null) script.close();
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public void handleNotification(final NotificationEvent event) {
        final ResourceAdapterScript script = get();
        if(script != null) script.handleNotification(event);
    }
}