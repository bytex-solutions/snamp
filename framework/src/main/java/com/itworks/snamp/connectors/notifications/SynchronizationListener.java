package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.concurrent.SynchronizationEvent;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Objects;

/**
 * Represents notification listener that can be used to handle the notification
 * synchronously. This class cannot be inherited.
 * <p>
 *     The following example demonstrates how to use this class:
 *     <pre>{@code
 *     final SynchronizationListener listener = new SynchronizationListener();
 *     final Object listenerId = connector.subscribe("subs-list", listener);
 *     listener.getAwaitor().await(); //blocks the caller thread
 *     connector.unsubscribe(listenerId);
 *     }</pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class SynchronizationListener extends SynchronizationEvent<Notification> implements NotificationListener {
    private static final String ANY_SUBSCRIPTION_LIST = "*";
    private final String expectedSubscriptionList;

    /**
     * Initializes a new synchronizer with the specified filter.
     * @param subscriptionList An identifier of the expected subscription list.
     */
    public SynchronizationListener(final String subscriptionList){
        super(true);
        expectedSubscriptionList = subscriptionList == null || subscriptionList.isEmpty() ?
                ANY_SUBSCRIPTION_LIST: subscriptionList;
    }

    /**
     * Initializes a new synchronizer for the notification delivery process.
     */
    public SynchronizationListener(){
        this(ANY_SUBSCRIPTION_LIST);
    }

    /**
     * Handles the specified notification.
     *
     * @param n        The notification to handle.
     * @param handback A custom object associated with notification.
     */
    @Override
    public void handleNotification(final Notification n, final Object handback) {
        if (Objects.equals(n.getType(), expectedSubscriptionList) ||
                Objects.equals(expectedSubscriptionList, ANY_SUBSCRIPTION_LIST))
            fire(n);
    }
}
