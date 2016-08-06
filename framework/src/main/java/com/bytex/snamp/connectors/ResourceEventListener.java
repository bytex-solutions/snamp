package com.bytex.snamp.connectors;

import java.util.EventListener;

/**
 * Represents a listener for all system events raised by managed resource adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ResourceEventListener extends EventListener {
    /**
     * Handles resource event.
     * @param event An event to handle.
     * @see com.bytex.snamp.connectors.FeatureAddedEvent
     * @see FeatureRemovingEvent
     */
    void handle(final ResourceEvent event);
}
