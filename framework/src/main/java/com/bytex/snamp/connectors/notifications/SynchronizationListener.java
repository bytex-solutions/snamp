package com.bytex.snamp.connectors.notifications;

import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.google.common.base.Strings;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

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
public final class SynchronizationListener extends SynchronizationEvent<Notification> implements NotificationListener, NotificationFilter {
    private static final String ANY_SUBSCRIPTION_LIST = "*";
    private static final long serialVersionUID = -5267013562476814414L;
    private final String expectedSubscriptionList;

    /**
     * Initializes a new synchronizer with the specified filter.
     * @param subscriptionList An identifier of the expected subscription list.
     */
    public SynchronizationListener(final String subscriptionList){
        super(true);
        expectedSubscriptionList = Strings.isNullOrEmpty(subscriptionList) ?
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
        if (isNotificationEnabled(n)) fire(n);
    }

    /**
     * Invoked before sending the specified notification to the listener.
     *
     * @param n The notification to be sent.
     * @return <CODE>true</CODE> if the notification has to be sent to the listener, <CODE>false</CODE> otherwise.
     */
    @Override
    public boolean isNotificationEnabled(final Notification n) {
        return expectedSubscriptionList.equals(n.getType()) ||
                ANY_SUBSCRIPTION_LIST.equals(expectedSubscriptionList);
    }
}
