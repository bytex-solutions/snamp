package com.snamp.connectors.util;

import com.snamp.connectors.*;

/**
 * Represents base implementation of the notification listener that simplify
 * the lifecycle of listeners for the single management connector.
 * <p>
 *     The following example demonstrates how to use this class for organizing listeners:<br/>
 *     <pre>{@code
 *      final AbstractNotificationListener<Notification> listener = new AbstractNotificationListener<>("failures"){
 *        @Override
 *        public boolean handle(final Notification n){
 *          System.out.println("%s: SEVERITY: %s: CONTENT: %s" n.getTimeStamp(), n.getSeverity(), n.getContent().toString());
 *        }
 *      }
 *      listener.attachTo(connector);
 *      //some operations with connector
 *      listener.detachFrom(connector);
 *     }</pre>
 * </p>
 * <p>
 *     This class is very useful for organizing unicast subscriptions.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractNotificationListener<N extends Notification> implements NotificationListener<N> {
    private Object listenerId;
    /**
     * Represents category of the event for which this listener is instantiated.
     */
    protected final String category;

    /**
     * Initializes a new listener for the specified category.
     * @param category The category of notifications to listen.
     */
    protected AbstractNotificationListener(final String category){
        this.category = category;
    }

    /**
     * Determines whether this listener is attached to the management connector.
     * @return {@literal true}, if this listener is attached to the management connector; otherwise, {@literal false}.
     */
    public final boolean isAttached(){
        return listenerId != null;
    }

    /**
     * Attaches this listener to the specified management connector.
     * @param connector The notification subscription provider. Cannot be {@literal null}.
     * @throws IllegalStateException This listener was previously attached to the management connector.
     */
    public synchronized final void attachTo(final ManagementConnector connector){
        if(isAttached()) throw new IllegalStateException(String.format("This listener is already attached with %s identifier.", listenerId));
        this.listenerId = connector.subscribe(category, this);
    }

    /**
     * Detaches this listener from the specified management connector.
     * @param connector The notification subscription provider. Cannot be {@literal null}.
     * @throws IllegalStateException This listener was not previously attached to the connector.
     */
    public synchronized final void detachFrom(final ManagementConnector connector){
        if(this.listenerId == null) throw new IllegalArgumentException("This listener already detached.");
        if(connector.unsubscribe(this)) this.listenerId = null;
    }
}
