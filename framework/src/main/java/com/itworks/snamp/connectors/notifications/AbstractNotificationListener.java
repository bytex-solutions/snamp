package com.itworks.snamp.connectors.notifications;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Objects;

/**
 * Represents base implementation of the notification listener that simplify
 * the lifecycle of listeners for the single management connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractNotificationListener implements NotificationListener {

    /**
     * Represents subscription list which is listened by this instance.
     */
    protected final String subscriptionList;

    /**
     * Initializes a new listener for the specified subscription list.
     * @param subscriptionList The subscription list.
     */
    protected AbstractNotificationListener(final String subscriptionList){
        if(subscriptionList == null || subscriptionList.isEmpty()) throw new IllegalArgumentException("Subscription list is null or empty.");
        this.subscriptionList = subscriptionList;
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notif The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public final void handleNotification(final Notification notif, final Object handback) {
        if(Objects.equals(subscriptionList, notif.getType()))
            handle(notif);
    }

    /**
     * Processes filtered notification.
     * @param notif The notification to process.
     */
    public abstract void handle(final Notification notif);
}
