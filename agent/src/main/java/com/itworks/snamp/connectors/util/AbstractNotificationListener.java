package com.itworks.snamp.connectors.util;

import com.itworks.snamp.connectors.NotificationMetadata;
import com.itworks.snamp.connectors.NotificationSupport;
import com.itworks.snamp.connectors.NotificationSupport.NotificationListener;
import com.itworks.snamp.connectors.NotificationSupport.Notification;

import java.util.concurrent.atomic.AtomicLong;

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
    private static final AtomicLong idCounter = new AtomicLong(0L);
    /**
     * Represents unique identifier of this listener.
     */
    protected final String listenerId;
    private NotificationMetadata metadata;

    /**
     * Represents identifier of the subscription list.
     */
    protected final String subscriptionList;

    /**
     * Initializes a new listener and associates it with the specified subscription list.
     * <p>
     *     Listener identifier will be automatically generated.
     * </p>
     * @param listId An identifier of the subscription list.
     */
    protected AbstractNotificationListener(final String listId){
        this(listId, Long.toString(idCounter.getAndIncrement()));
    }

    /**
     * Initializes a new listener and associates it with the specified subscription list.
     * @param listId An identifier of the subscription list.
     * @param listenerId An identifier of the listener.
     */
    protected AbstractNotificationListener(final String listId, final String listenerId){
        this.subscriptionList = listId;
        this.listenerId = listenerId;
    }

    /**
     * Returns the subscription list identifier.
     * @return The subscription list identifier passed to {@link #AbstractNotificationListener(String)} constructor.
     */
    public final String getSubscriptionListId(){
        return subscriptionList;
    }

    /**
     * Returns the listener identifier.
     * @return The listener identifier.
     */
    public final String getListenerId(){
        return listenerId;
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
        return metadata != null;
    }

    /**
     * Attaches this listener to the specified management connector.
     * @param connector The notification subscription provider. Cannot be {@literal null}.
     * @return {@literal true}, if this listener successfully attached to the connector; otherwise, {@literal false}.
     */
    public synchronized final boolean attachTo(final NotificationSupport connector){
        if(isAttached()) return false;
        else if(connector.subscribe(listenerId, subscriptionList, this)){
            this.metadata = connector.getNotificationInfo(subscriptionList);
            return true;
        }
        else return false;
    }

    /**
     * Detaches this listener from the specified management connector.
     * @param connector The notification subscription provider. Cannot be {@literal null}.
     */
    public synchronized final boolean detachFrom(final NotificationSupport connector){
        if(connector.unsubscribe(listenerId)){
            this.metadata = null;
            return true;
        }
        else return false;
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
                    public boolean handle(final Notification n, final String category) {
                        return listener.handle(n, category);
                    }
                };
    }
}
