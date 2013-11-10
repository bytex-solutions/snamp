package com.snamp.connectors.util;

import com.snamp.connectors.Notification;
import com.snamp.connectors.NotificationListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Helps to collect notification into a single collection.
 * @param <N> Type of the notifications that can be stored in this collection.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationInputBox<N extends Notification> extends ArrayList<N> implements NotificationListener<N> {
    /**
     * Initializes a new empty collection for notifications.
     */
    public NotificationInputBox(){
        super(10);
    }

    /**
     * Pushes the notification
     *
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean handle(final N n) {
        add(n);
        return true;
    }
}
