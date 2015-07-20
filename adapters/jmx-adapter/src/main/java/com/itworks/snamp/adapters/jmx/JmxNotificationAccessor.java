package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.NotificationAccessor;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.ref.WeakReference;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxNotificationAccessor extends NotificationAccessor {
    private final String resourceName;
    private final WeakReference<NotificationListener> listenerRef;

    JmxNotificationAccessor(final String resourceName,
                            final MBeanNotificationInfo metadata,
                            final NotificationListener destination) {
        super(metadata);
        this.resourceName = resourceName;
        listenerRef = new WeakReference<>(destination);
    }

    MBeanNotificationInfo cloneMetadata() {
        return new MBeanNotificationInfo(getMetadata().getNotifTypes(),
                getMetadata().getName(),
                getMetadata().getDescription(),
                getMetadata().getDescriptor());
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(resourceName);
        final NotificationListener listener = listenerRef.get();
        if (listener != null) listener.handleNotification(notification, handback);
    }
}
