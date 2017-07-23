package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationSet;
import com.bytex.snamp.gateway.modeling.ResourceFeatureList;
import com.bytex.snamp.gateway.modeling.ResourceNotificationList;
import com.google.common.collect.ImmutableList;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class XMPPModelOfNotifications extends ConcurrentResourceAccessor<Map<String, ResourceNotificationList<XMPPNotificationAccessor>>> implements NotificationSet<XMPPNotificationAccessor>, NotificationListener {
    private static final long serialVersionUID = 8057114297838181532L;
    private final WeakEventListenerList<NotificationListener, NotificationEvent> listeners;

    XMPPModelOfNotifications(){
        super(new HashMap<>(10));
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        listeners = new WeakEventListenerList<>(NotificationListener::handleNotification);
    }

    /**
     * Adds a new notification listener to this collection.
     * @param listener A new notification listener to add. Cannot be {@literal null}.
     */
    public void addNotificationListener(final NotificationListener listener) {
        listeners.add(listener);
    }

    public boolean removeNotificationListener(final NotificationListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public void handleNotification(final NotificationEvent event) {
        listeners.fire(event);
    }

    XMPPNotificationAccessor enableNotifications(final String resourceName,
                             final MBeanNotificationInfo metadata) throws InterruptedException, TimeoutException {
        return write(notifications -> {
            final ResourceNotificationList<XMPPNotificationAccessor> resource;
            if (notifications.containsKey(resourceName))
                resource = notifications.get(resourceName);
            else notifications.put(resourceName, resource = new ResourceNotificationList<>());
            final XMPPNotificationAccessor router = new XMPPNotificationAccessor(metadata,
                    this,
                    resourceName);
            resource.put(router);
            return router;
        }, null);
    }

    XMPPNotificationAccessor disableNotifications(final String resourceName,
                                              final MBeanNotificationInfo metadata) throws InterruptedException, TimeoutException {
        return write(notifications -> {
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.get(resourceName);
            if (resource == null) return null;
            final XMPPNotificationAccessor accessor = resource.remove(metadata);
            if (resource.isEmpty())
                notifications.remove(resourceName);
            return accessor;
        }, null);
    }

    Collection<XMPPNotificationAccessor> clear(final String resourceName) throws InterruptedException, TimeoutException {
        return write(notifications -> {
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.remove(resourceName);
            return resource != null ? resource.values() : ImmutableList.of();
        }, null);
    }

    void clear() throws InterruptedException, TimeoutException {
        listeners.clear();
        write(notifications -> {
            notifications.values().forEach(ResourceFeatureList::clear);
            notifications.clear();
            return null;
        }, null);
    }

    @Override
    public <E extends Throwable> boolean forEachNotification(final EntryReader<String, ? super XMPPNotificationAccessor, E> notificationReader) throws E {
        return read(notifications -> {
            for (final ResourceNotificationList<XMPPNotificationAccessor> list : notifications.values())
                for (final XMPPNotificationAccessor accessor : list.values())
                    if (!notificationReader.accept(accessor.getResourceName(), accessor))
                        return false;
            return true;
        });
    }
}
