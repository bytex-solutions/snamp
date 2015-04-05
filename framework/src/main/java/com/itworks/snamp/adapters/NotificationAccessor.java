package com.itworks.snamp.adapters;

import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.connectors.notifications.TypeBasedNotificationFilter;
import com.itworks.snamp.internal.annotations.MethodStub;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * Exposes access to the individual notification.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class NotificationAccessor extends FeatureAccessor<MBeanNotificationInfo, NotificationSupport> implements NotificationListener {
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
    final void connect(final NotificationSupport value) {
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
    public final void disconnect() {
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
}
