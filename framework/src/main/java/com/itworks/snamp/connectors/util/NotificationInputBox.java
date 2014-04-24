package com.itworks.snamp.connectors.util;

import com.itworks.snamp.connectors.NotificationSupport.Notification;
import com.itworks.snamp.connectors.NotificationSupport.NotificationListener;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Helps to collect notification into a single collection.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationInputBox extends ConcurrentLinkedQueue<Notification> implements NotificationListener {
    private static final String ANY_SUBSCRIPTION_LIST = "*";
    static final long serialVersionUID = 34444;

    private final String subscriptionList;

    /**
     * Initializes a new empty collection of notifications.
     */
    @SuppressWarnings("UnusedDeclaration")
    public NotificationInputBox(){
        this(ANY_SUBSCRIPTION_LIST);
    }

    /**
     * Initializes a new empty collection of notifications with the specified filter.
     * @param listId A filter for subscription list.
     */
    public NotificationInputBox(final String listId){
        this.subscriptionList = listId == null || listId.isEmpty() ? "*" : listId;
    }

    /**
     * Pushes the notification
     *
     * @param listId Subscription list source.
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean handle(final String listId, final Notification n) {
        if(Objects.equals(listId, subscriptionList) || Objects.equals(ANY_SUBSCRIPTION_LIST, listId)) {
            add(n);
            return true;
        }
        else return true;
    }
}
