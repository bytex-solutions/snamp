package com.snamp.connectors.util;

import com.snamp.connectors.*;

import java.util.HashMap;

/**
 * Represents a list of subscribed notification listeners.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SubscriptionList extends HashMap<Object, NotificationListener<? extends Notification>> {

    /**
     * Removes the notification listener from the specified connector.
     * @param connector The management connector which contains the subscribed listener.
     * @param listenerId An identifier of the subscribed listener.
     * @return An instance of the listener that was removed from subscription; or {@literal null}
     * if the specified listener identifier is invalid for the specified management connector.
     */
    public final NotificationListener<? extends Notification> unsubscribe(final ManagementConnector connector, final Object listenerId){
        return connector.unsubscribe(listenerId) ? remove(listenerId) : null;
    }
}
