package com.snamp.connectors;

import java.util.*;

/**
 * Represents notification listener.
 * @param <N> Type of the notification message to handle.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationListener<N extends Notification> extends EventListener {

    /**
     * Handles the specified notification.
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    public boolean handle(final N n);
}
