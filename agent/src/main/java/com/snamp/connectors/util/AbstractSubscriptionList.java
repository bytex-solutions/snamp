package com.snamp.connectors.util;

import com.snamp.connectors.*;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import static com.snamp.connectors.NotificationSupport.NotificationListener;

import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import java.util.*;

/**
 * Represents a base class for constructing subscription lists.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSubscriptionList extends HashMap<String, EnabledNotification> {

    /**
     * Initializes a new empty list of notification listeners.
     */
    protected AbstractSubscriptionList(){
        super(10);
    }

    /**
     * Creates a new holder for the event listeners.
     * @param connector
     * @return A new instance of subscription list.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected abstract EnabledNotification createBinding(final NotificationSupport connector);

    /**
     * Enables notifications for all specified events.
     * @param connector An event emitter.
     * @param namespace An event namespace.
     * @param events A collection of events to enable.
     * @return A collection of enabled notifications (its postfixes).
     */
    public final Collection<String> putAll(final NotificationSupport connector, final String namespace, final Map<String, EventConfiguration> events){
        final Set<String> enabledNotifs = new HashSet<>(events.size());
        for(final String postfix: events.keySet()){
            final EnabledNotification notifs;
            if(containsKey(namespace))
                notifs = get(namespace);
            else put(namespace, notifs = createBinding(connector));
            final EventConfiguration evConfig = events.get(postfix);
            final NotificationMetadata metadata = connector.enableNotifications(notifs.makeListId(namespace, postfix), evConfig.getCategory(), evConfig.getAdditionalElements());
            //notifications for the specified category is not supported
            if(metadata == null)
                remove(namespace);
            else {
                notifs.put(postfix, metadata);
                enabledNotifs.add(postfix);
            }
        }
        return enabledNotifs;
    }

    /**
     * Represents event subscription.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface Subscription{
        /**
         * Removes this subscription.
         * @return {@literal true}, if subscription is removed; otherwise, {@literal false}.
         */
        public boolean unsubscribe();

        /**
         * Returns a listener identifier associated with this subscription.
         * @return An identifier of the listener returned by {@link com.snamp.connectors.NotificationSupport#subscribe(String, com.snamp.connectors.NotificationSupport.NotificationListener)},
         */
        public Object getListenerId();
    }

    /**
     * Creates subscription for the specified event.
     * @param namespace An event prefix.
     * @param postfix An event postfix.
     * @param listener A listener to add.
     * @return A listener identifier.
     */
    public final Subscription subscribe(final String namespace, final String postfix, final NotificationListener listener){
        if(containsKey(namespace)){
            final EnabledNotification notifications = get(namespace);
            if(notifications.containsKey(postfix)){
                final Object listenerId = notifications.subscribe(notifications.makeListId(namespace, postfix), listener);
                return new Subscription() {
                    @Override
                    public final boolean unsubscribe() {
                        return notifications.unsubscribe(listenerId);
                    }

                    @Override
                    public final Object getListenerId() {
                        return listenerId;
                    }
                };
            }
            else return null;
        }
        else return null;
    }
}
