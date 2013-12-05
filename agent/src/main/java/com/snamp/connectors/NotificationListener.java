package com.snamp.connectors;

import java.util.*;

/**
 * Represents notification listener.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationListener extends EventListener {

    /**
     * Handles the specified notification.
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    public boolean handle(final Notification n);
}
