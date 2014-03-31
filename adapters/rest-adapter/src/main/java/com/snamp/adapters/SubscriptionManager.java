package com.snamp.adapters;

import com.snamp.connectors.NotificationSupport.NotificationListener;
import com.snamp.connectors.util.AbstractSubscriptionList.Subscription;

import java.util.Map;

/**
 * Represents subscription manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see SubscriptionList
 */
interface SubscriptionManager {
    public Map<String, Subscription<?>> subscribeToAll(final NotificationListener listener);
}
