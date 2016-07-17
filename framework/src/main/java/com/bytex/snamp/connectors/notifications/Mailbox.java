package com.bytex.snamp.connectors.notifications;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.concurrent.BlockingQueue;

/**
 * Represents mailbox for JMX notifications.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public interface Mailbox extends BlockingQueue<Notification>, NotificationListener, NotificationFilter {
    @Override
    default void handleNotification(final Notification notification, final Object handback){
        offer(notification);
    }

    @Override
    default boolean isNotificationEnabled(Notification notification){
        return true;
    }
}
