package com.itworks.snamp.adapters;

import com.itworks.snamp.connectors.NotificationSupport;
import static com.itworks.snamp.connectors.NotificationSupport.NotificationListener;

import com.itworks.snamp.connectors.util.AbstractSubscriptionList;
import com.itworks.snamp.connectors.util.EnabledNotifications;

import java.util.*;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

/**
 * Represents implementation of the abstract subscription list for REST adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SubscriptionList extends AbstractSubscriptionList<EventConfiguration> {
    /**
     * Creates a new holder for the event listeners.
     *
     * @param connector
     * @return A new instance of subscription list.
     */
    @Override
    protected EnabledNotifications<EventConfiguration> createBinding(final NotificationSupport connector) {
        return new EnabledNotifications<EventConfiguration>(connector) {
            @Override
            public String makeListId(final String prefix, final String postfix) {
                return RestAdapterHelpers.makeEventID(prefix, postfix);
            }

            @Override
            public EventConfiguration createDescription(final String prefix, final String postfix, final EventConfiguration config) {
                return config;
            }
        };
    }

    private static SubscriptionManager createSubscriptionManager(final AbstractSubscriptionList subscriptionList){
        return new SubscriptionManager() {
            @Override
            public synchronized final Map<String, Subscription<?>> subscribeToAll(final NotificationListener listener) {
                return subscriptionList.subscribeToAll(listener);
            }
        };
    }

    public final SubscriptionManager createSubscriptionManager(){
        return createSubscriptionManager(this);
    }
}
