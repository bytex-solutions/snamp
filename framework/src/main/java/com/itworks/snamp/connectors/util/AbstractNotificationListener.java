package com.itworks.snamp.connectors.util;

import com.itworks.snamp.connectors.NotificationMetadata;

import java.util.Objects;

import static com.itworks.snamp.connectors.NotificationSupport.*;

/**
 * Represents base implementation of the notification listener that simplify
 * the lifecycle of listeners for the single management connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractNotificationListener implements NotificationListener {
    /**
     * Represents metadata of the notification to listen.
     */
    protected final NotificationMetadata notificationInfo;

    /**
     * Represents subscription list which is listened by this instance.
     */
    protected final String subscriptionList;

    /**
     * Initializes a new listener for the specified subscription list.
     * @param info Metadata of the notification to listen.
     * @param subscriptionList The subscription list.
     */
    protected AbstractNotificationListener(final NotificationMetadata info, final String subscriptionList){
        if(info == null) throw new IllegalArgumentException("info");
        else if(subscriptionList == null || subscriptionList.isEmpty()) throw new IllegalArgumentException("Subscription list is null or empty.");
        else {
            this.notificationInfo = info;
            this.subscriptionList = subscriptionList;
        }
    }

    /**
     * Handles the incoming notification.
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    protected abstract boolean handle(final Notification n);

    /**
     * Handles the specified notification.
     *
     * @param listId An identifier of the subscription list.
     * @param n      The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean handle(final String listId, final Notification n) {
        return Objects.equals(listId, subscriptionList) && handle(n);
    }
}
