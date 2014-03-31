package com.snamp.connectors.util;

import com.snamp.connectors.*;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import static com.snamp.connectors.NotificationSupport.NotificationListener;

import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import java.util.*;

/**
 * Represents a base class for constructing subscription lists.
 * @param <TNotificationDescriptor> Represents notification descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSubscriptionList<TNotificationDescriptor> extends HashMap<String, EnabledNotifications<TNotificationDescriptor>> {
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
    protected abstract EnabledNotifications<TNotificationDescriptor> createBinding(final NotificationSupport connector);

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
            final EnabledNotifications<TNotificationDescriptor> notifs;
            if(containsKey(namespace))
                notifs = get(namespace);
            else put(namespace, notifs = createBinding(connector));
            final EventConfiguration evConfig = events.get(postfix);
            final NotificationMetadata metadata = connector.enableNotifications(notifs.makeListId(namespace, postfix), evConfig.getCategory(), evConfig.getAdditionalElements());
            //notifications for the specified category is not supported
            if(metadata == null)
                remove(namespace);
            else {
                final TNotificationDescriptor descriptor = notifs.createDescription(namespace, postfix, evConfig);
                if(descriptor != null){
                    notifs.put(postfix, descriptor);
                    enabledNotifs.add(postfix);
                }
            }
        }
        return enabledNotifs;
    }

    /**
     * Retrieves notification metadata.
     * @param prefix Notification target prefix.
     * @param postfix Notification postfix.
     * @return The notification metadata.
     */
    public final NotificationMetadata getNotificationInfo(final String prefix, final String postfix){
        final EnabledNotifications notifications = get(prefix);
        return notifications != null ?
                notifications.getNotificationInfo(prefix, postfix):
                null;
    }

    /**
     * Represents event subscription.
     * @param <TNotificationDescriptor> The descriptor of the notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface Subscription<TNotificationDescriptor>{
        /**
         * Removes this subscription.
         * @return {@literal true}, if subscription is removed; otherwise, {@literal false}.
         */
        public boolean unsubscribe();

        /**
         * Returns a listener identifier associated with this subscription.
         * @return Unique identifier of the notification listener generated automatically.
         */
        public String getListenerId();

        /**
         * Gets the descriptor of the notification to which this subscription is bounded.
         * @return The descriptor of the notification to which this subscription is bounded.
         */
        public TNotificationDescriptor getDescriptor();
    }

    /**
     * Creates subscription for the specified event.
     * @param namespace An event prefix.
     * @param postfix An event postfix.
     * @param listener A listener to add.
     * @return A listener identifier.
     */
    public final Subscription<TNotificationDescriptor> subscribe(final String namespace, final String postfix, final NotificationListener listener){
        if(containsKey(namespace)){
            final EnabledNotifications<TNotificationDescriptor> notifications = get(namespace);
            if(notifications.containsKey(postfix)){
                final String listenerId = notifications.subscribe(notifications.makeListId(namespace, postfix), listener);
                return listenerId != null ? new Subscription<TNotificationDescriptor>() {
                    @Override
                    public final boolean unsubscribe() {
                        return notifications.unsubscribe(listenerId);
                    }

                    @Override
                    public final String getListenerId() {
                        return listenerId;
                    }

                    /**
                     * Gets the descriptor of the notification to which this subscription is bounded.
                     *
                     * @return The descriptor of the notification to which this subscription is bounded.
                     */
                    @Override
                    public TNotificationDescriptor getDescriptor() {
                        return notifications.get(postfix);
                    }
                } : null;
            }
            else return null;
        }
        else return null;
    }


    public final <T extends EnabledNotifications<TNotificationDescriptor>> T get(final String prefix, final Class<T> classInfo){
        final EnabledNotifications<TNotificationDescriptor> result = get(prefix);
        return classInfo.isInstance(result) ? classInfo.cast(result) : null;
    }

    /**
     * Disables all notifications.
     */
    public final void disable(){
        for(final Map.Entry<String, EnabledNotifications<TNotificationDescriptor>> entry: entrySet())
            entry.getValue().disable(entry.getKey());
    }

    public final Map<String, Subscription<TNotificationDescriptor>> subscribeToAll(final NotificationListener listener) {
        final Map<String, Subscription<TNotificationDescriptor>> result = new HashMap<>(10);
        for(final String prefix: keySet()){
            final EnabledNotifications<TNotificationDescriptor> notifs = get(prefix);
            for(final String postfix: notifs.keySet())
                result.put(notifs.makeListId(prefix, postfix), subscribe(prefix, postfix, listener));
        }
        return result;
    }
}
