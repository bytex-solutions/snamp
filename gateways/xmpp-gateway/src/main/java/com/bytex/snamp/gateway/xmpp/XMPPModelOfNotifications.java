package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.gateway.modeling.MulticastNotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationSet;
import com.bytex.snamp.gateway.modeling.ResourceFeatureList;
import com.bytex.snamp.gateway.modeling.ResourceNotificationList;
import com.google.common.collect.ImmutableList;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static com.bytex.snamp.concurrent.LockManager.lockAndAccept;
import static com.bytex.snamp.concurrent.LockManager.lockAndApply;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class XMPPModelOfNotifications extends MulticastNotificationListener implements NotificationSet<XMPPNotificationAccessor> {
    private final Map<String, ResourceNotificationList<XMPPNotificationAccessor>> notifications;
    private final ReadWriteLock lock;

    XMPPModelOfNotifications(){
        notifications = new HashMap<>(10);
        lock = new ReentrantReadWriteLock();
    }

    private XMPPNotificationAccessor enableNotificationsImpl(final String resourceName,
                                                 final MBeanNotificationInfo metadata) {
        final ResourceNotificationList<XMPPNotificationAccessor> resource;
        if (notifications.containsKey(resourceName))
            resource = notifications.get(resourceName);
        else notifications.put(resourceName, resource = new ResourceNotificationList<>());
        final XMPPNotificationAccessor router = new XMPPNotificationAccessor(metadata,
                this,
                resourceName);
        resource.put(router);
        return router;
    }

    XMPPNotificationAccessor enableNotifications(final String resourceName,
                             final MBeanNotificationInfo metadata) throws InterruptedException {
        return lockAndApply(lock.writeLock(), resourceName, metadata, this::enableNotificationsImpl, Function.identity());
    }

    private XMPPNotificationAccessor disableNotificationsImpl(final String resourceName,
                                                  final MBeanNotificationInfo metadata) {
        final ResourceNotificationList<XMPPNotificationAccessor> resource =
                notifications.get(resourceName);
        if (resource == null) return null;
        final XMPPNotificationAccessor accessor = resource.remove(metadata);
        if (resource.isEmpty())
            notifications.remove(resourceName);
        return accessor;

    }

    XMPPNotificationAccessor disableNotifications(final String resourceName,
                                              final MBeanNotificationInfo metadata) throws InterruptedException {
        return lockAndApply(lock.writeLock(), resourceName, metadata, this::disableNotificationsImpl, Function.identity());
    }

    private Collection<XMPPNotificationAccessor> clearImpl(final String resourceName) {
        final ResourceNotificationList<XMPPNotificationAccessor> resource =
                notifications.remove(resourceName);
        return resource != null ? resource.values() : ImmutableList.of();
    }

    Collection<XMPPNotificationAccessor> clear(final String resourceName) throws InterruptedException {
        return lockAndApply(lock.writeLock(), this, resourceName, XMPPModelOfNotifications::clearImpl, Function.identity());
    }

    void clear() throws InterruptedException {
        removeAll();
        lockAndAccept(lock.writeLock(), notifications, notifs -> {
            notifs.values().forEach(ResourceFeatureList::clear);
            notifs.clear();
        }, Function.identity());
    }

    private  <E extends Throwable> void forEachNotificationImpl(final EntryReader<String, ? super XMPPNotificationAccessor, E> notificationReader) throws E {
        for (final ResourceNotificationList<XMPPNotificationAccessor> list : notifications.values())
            for (final XMPPNotificationAccessor accessor : list.values())
                if (!notificationReader.accept(accessor.resourceName, accessor)) return;

    }

    @Override
    public <E extends Throwable> void forEachNotification(final EntryReader<String, ? super XMPPNotificationAccessor, E> notificationReader) throws E {
        lockAndAccept(lock.readLock(), notificationReader, this::forEachNotificationImpl);
    }
}
