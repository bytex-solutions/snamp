package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.*;

import java.lang.ref.*;
import java.util.HashMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class EnabledNotification extends HashMap<String, NotificationMetadata> {
   private final NotificationSupport connector;

    protected EnabledNotification(final NotificationSupport connector){
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
}
