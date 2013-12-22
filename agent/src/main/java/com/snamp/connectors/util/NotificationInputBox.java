package com.snamp.connectors.util;

import com.snamp.connectors.NotificationSupport.Notification;
import com.snamp.connectors.NotificationSupport.NotificationListener;

import java.util.ArrayList;

/**
 * Helps to collect notification into a single collection.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationInputBox extends ArrayList<Notification> implements NotificationListener {
    static final long serialVersionUID = 34444;

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
    public final boolean handle(final Notification n) {
        add(n);
        return true;
    }
}
