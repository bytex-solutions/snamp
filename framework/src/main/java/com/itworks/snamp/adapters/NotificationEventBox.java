package com.itworks.snamp.adapters;

import java.util.concurrent.ArrayBlockingQueue;
import static com.itworks.snamp.internal.Utils.blackhole;

/**
 * Represents alternative queue of notifications with limited size.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see com.itworks.snamp.connectors.notifications.NotificationBox
 */
public class NotificationEventBox extends ArrayBlockingQueue<NotificationEvent> implements NotificationListener {
    private static final long serialVersionUID = -5820313619999992284L;

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param maxCapacity the capacity of this queue
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public NotificationEventBox(final int maxCapacity) {
        super(maxCapacity);
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public void handleNotification(final NotificationEvent event) {
        blackhole(offer(event));
    }
}