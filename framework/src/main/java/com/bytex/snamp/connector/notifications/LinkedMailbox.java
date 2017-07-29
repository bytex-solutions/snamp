package com.bytex.snamp.connector.notifications;

import javax.management.Notification;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

/**
 * Mailbox with unbounded capacity.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.2
 */
final class LinkedMailbox extends LinkedBlockingQueue<Notification> implements Mailbox {
    private static final long serialVersionUID = 6730199405668697834L;
    private final Predicate<? super Notification> filter;

    LinkedMailbox() {
        this(n -> true);
    }

    LinkedMailbox(final Predicate<? super Notification> filter){
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        offer(notification);
    }

    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return filter.test(notification);
    }
}
