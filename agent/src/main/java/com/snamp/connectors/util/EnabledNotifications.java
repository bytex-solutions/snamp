package com.snamp.connectors.util;

import com.snamp.connectors.*;
import com.snamp.connectors.NotificationSupport.NotificationListener;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import java.util.HashMap;

/**
 * @param <TNotificationDescriptor> Represents notification descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class EnabledNotifications<TNotificationDescriptor> extends HashMap<String, TNotificationDescriptor> {
    /**
     * Represents underlying connector.
     */
    protected final NotificationSupport connector;

    protected EnabledNotifications(final NotificationSupport connector){
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        this.connector = connector;
    }

    Object subscribe(final String listId, final NotificationListener listener){
        return connector.subscribe(listId, listener);
    }

    boolean unsubscribe(final Object listenerId){
        return connector.unsubscribe(listenerId);
    }

    /**
     * Constructs a new identifier of the subscription list.
     * @param prefix An event namespace (namespace in management target configuration).
     * @param postfix An event identifier (id in event configuration).
     * @return A new subscription list identifier.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract String makeListId(final String prefix, final String postfix);

    /**
     * Disables all notifications associated with the specified prefix.
     * @param prefix The prefix of the notification to disable.
     */
    public final void disable(final String prefix){
        for(final String postfix: keySet())
            connector.disableNotifications(makeListId(prefix, postfix));
    }

    /**
     * Constructs a new notification descriptor.
     * @param prefix An event namespace (namespace in management target configuration).
     * @param postfix An event identifier (id in event configuration).
     * @param config An information about enabled notification.
     * @return A new subscription list identifier.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract TNotificationDescriptor createDescription(final String prefix, final String postfix, final EventConfiguration config);

    NotificationMetadata getNotificationInfo(final String prefix, final String postfix){
        return connector.getNotificationInfo(makeListId(prefix, postfix));
    }
}
