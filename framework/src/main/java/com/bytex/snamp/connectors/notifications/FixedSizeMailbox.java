package com.bytex.snamp.connectors.notifications;

import javax.management.Notification;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents fixed-size mailbox for the notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class FixedSizeMailbox extends ArrayBlockingQueue<Notification> implements Mailbox {
    private static final long serialVersionUID = 7011237951803102271L;

    /**
     * Initializes a new mailbox.
     * @param maxCapacity The capacity of the new mailbox.
     */
    FixedSizeMailbox(final int maxCapacity){
        super(maxCapacity);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        offer(notification);
    }
}
