package com.bytex.snamp.connector;

import javax.annotation.Nonnull;
import java.util.EventListener;

/**
 * Represents a listener for all system events raised by gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
public interface ResourceEventListener extends EventListener {
    /**
     * Handles resource event.
     * @param event An event to handle.
     * @see FeatureModifiedEvent
     */
    void resourceModified(@Nonnull final ResourceEvent event);
}
