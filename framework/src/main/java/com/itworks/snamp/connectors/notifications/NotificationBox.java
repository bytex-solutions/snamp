package com.itworks.snamp.connectors.notifications;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents fixed-size mailbox for the notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationBox extends ArrayBlockingQueue<Notification> implements NotificationListener {
    /**
     * Initializes a new mailbox.
     * @param maxCapacity The capacity of the new mailbox.
     */
    public NotificationBox(final int maxCapacity){
        super(maxCapacity);
    }


    /**
     * Save the specified notification into this mailbox.
     *
     * @param listId An identifier of the subscription list.
     * @param n      The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean handle(final String listId, final Notification n) {
        return offer(n);
    }
}
