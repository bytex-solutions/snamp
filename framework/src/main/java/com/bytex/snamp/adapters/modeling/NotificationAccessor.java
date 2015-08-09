package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.connectors.FeatureModifiedEvent;
import com.bytex.snamp.connectors.notifications.NotificationAddedEvent;
import com.bytex.snamp.connectors.notifications.NotificationRemovingEvent;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.TypeBasedNotificationFilter;
import com.bytex.snamp.internal.annotations.MethodStub;

import javax.management.*;

/**
 * Exposes access to the individual notification.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class NotificationAccessor extends FeatureAccessor<MBeanNotificationInfo> implements NotificationListener {
    private NotificationSupport notificationSupport;

    /**
     * Initializes a new managed resource notification accessor.
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    protected NotificationAccessor(final MBeanNotificationInfo metadata) {
        super(metadata);
        this.notificationSupport = null;
    }

    @Override
    public final boolean processEvent(final FeatureModifiedEvent<MBeanNotificationInfo> event) {
        if(event instanceof NotificationAddedEvent) {
            connect(((NotificationAddedEvent) event).getSource());
            return true;
        }
        else if(event instanceof NotificationRemovingEvent) {
            close();
            return true;
        }
        else return false;
    }

    private void connect(final NotificationSupport value) {
        this.notificationSupport = value;
        if(value != null)
            value.addNotificationListener(this, createFilter(), null);
    }

    /**
     * Determines whether the feature of the managed resource is accessible
     * through this object.
     *
     * @return {@literal true}, if this feature is accessible; otherwise, {@literal false}.
     */
    @Override
    public final boolean isConnected() {
        return notificationSupport != null;
    }

    @MethodStub
    public void disconnected(){

    }

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    public final void close() {
        try {
            final NotificationSupport ns = this.notificationSupport;
            if(ns != null)
                ns.removeNotificationListener(this);
        }
        catch (ListenerNotFoundException ignored) {
        }
        finally {
            this.notificationSupport = null;
            disconnected();
        }
    }

    /**
     * Gets notification type.
     * @return The notification type.
     */
    public final String getType(){
        return getMetadata().getNotifTypes()[0];
    }

    /**
     * Creates a new notification filter for this type of the metadata.
     * @return A new notification filter.
     * @see javax.management.MBeanNotificationInfo#getNotifTypes()
     */
    public final NotificationFilter createFilter(){
        return new TypeBasedNotificationFilter(getMetadata());
    }

    public static int removeAll(final Iterable<? extends NotificationAccessor> notifications,
                             final MBeanNotificationInfo metadata){
        return FeatureAccessor.removeAll(notifications, metadata);
    }

    public static <N extends NotificationAccessor> N remove(final Iterable<N> attributes,
                                                         final MBeanNotificationInfo metadata){
        return FeatureAccessor.remove(attributes, metadata);
    }
}
