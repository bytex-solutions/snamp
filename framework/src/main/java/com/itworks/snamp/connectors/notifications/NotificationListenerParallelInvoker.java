package com.itworks.snamp.connectors.notifications;

import java.util.concurrent.ExecutorService;

/**
 * Represents notification listener invoker which invokes listener
 * in the separated task scheduled through {@link java.util.concurrent.ExecutorService}
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationListenerParallelInvoker extends NotificationListenerInvoker {
    /**
     * Gets notification listener scheduler.
     * @return The notification listener scheduler.
     */
    ExecutorService getScheduler();
}
