package com.snamp.connectors.util;

import com.snamp.connectors.*;
import com.snamp.connectors.NotificationSupport.NotificationListener;
import com.snamp.connectors.NotificationSupport.Notification;

/**
 * Represents base implementation of the notification listener that simplify
 * the lifecycle of listeners for the single management connector.
 * <p>
 *     The following example demonstrates how to use this class for organizing listeners:<br/>
 *     <pre><code>
 *      final AbstractNotificationListener<Notification> listener = new AbstractNotificationListener<>("failures"){
 *        {@literal @}Override
 *        public boolean handle(final Notification n){
 *          System.out.println("%s: SEVERITY: %s: CONTENT: %s" n.getTimeStamp(), n.getSeverity(), n.getContent().toString());
 *        }
 *      }
 *      listener.attachTo(connector);
 *      //some operations with connector
 *      listener.detachFrom(connector);
 *     </code></pre>
 * </p>
 * <p>
 *     This class is very useful for organizing unicast subscriptions.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractNotificationListener implements NotificationListener {
    private Object listenerId;
    private NotificationMetadata metadata;

    /**
     * Represents identifier of the subscription list.
     */
    protected final String subscriptionList;

    /**
     * Initializes a new listener and associates it with the specified subscription list.
     * @param listId An identifier of the subscription list.
     */
    protected AbstractNotificationListener(final String listId){
        this.subscriptionList = listId;
    }

    /**
     * Returns the subscription list identifier.
     * @return The subscription list identifier passed to {@link #AbstractNotificationListener(String)} constructor.
     */
    public final String getSubscriptionListId(){
        return subscriptionList;
    }

    /**
     * Returns a metadata of the notification.
     * @return The metadata of the notification; or {@literal null}, if listener is not attached.
     */
    protected final NotificationMetadata getMetadata(){
        return metadata;
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
    public synchronized final void attachTo(final NotificationSupport connector){
        if(isAttached()) throw new IllegalStateException(String.format("This listener is already attached with %s identifier.", listenerId));
        this.listenerId = connector.subscribe(subscriptionList, this);
        this.metadata = connector.getNotificationInfo(subscriptionList);
    }

    /**
     * Detaches this listener from the specified management connector.
     * @param connector The notification subscription provider. Cannot be {@literal null}.
     * @throws IllegalStateException This listener was not previously attached to the connector.
     */
    public synchronized final void detachFrom(final NotificationSupport connector){
        if(this.listenerId == null) throw new IllegalArgumentException("This listener already detached.");
        if(connector.unsubscribe(this)) {
            this.metadata = null;
            this.listenerId = null;
        }
    }

    /**
     * Wraps a notification listener into the implementation of this class.
     * @param listId An identifier of the subscription list.
     * @param listener A listener to wrap.
     * @return A new instance of this class that wraps the specified listener.
     */
    public final static AbstractNotificationListener wrap(final String listId, final NotificationListener listener){
        return listener instanceof AbstractNotificationListener ?
                (AbstractNotificationListener)listener:
                new AbstractNotificationListener(listId) {
                    @Override
                    public boolean handle(final Notification n) {
                        return listener.handle(n);
                    }
                };
    }
}
