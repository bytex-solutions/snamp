package com.bytex.snamp.adapters.xmpp;

import com.bytex.snamp.adapters.modeling.ResourceFeatureList;
import com.google.common.collect.ImmutableList;
import com.bytex.snamp.adapters.modeling.MulticastNotificationListener;
import com.bytex.snamp.adapters.modeling.NotificationSet;
import com.bytex.snamp.adapters.modeling.ResourceNotificationList;
import com.bytex.snamp.EntryReader;

import javax.management.MBeanNotificationInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class XMPPModelOfNotifications extends MulticastNotificationListener implements NotificationSet<XMPPNotificationAccessor> {
    private final Map<String, ResourceNotificationList<XMPPNotificationAccessor>> notifications;
    private final ReadWriteLock lock;

    XMPPModelOfNotifications(){
        notifications = new HashMap<>(10);
        lock = new ReentrantReadWriteLock();
    }

    XMPPNotificationAccessor enableNotifications(final String resourceName,
                             final MBeanNotificationInfo metadata) {
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            final ResourceNotificationList<XMPPNotificationAccessor> resource;
            if (notifications.containsKey(resourceName))
                resource = notifications.get(resourceName);
            else notifications.put(resourceName, resource = new ResourceNotificationList<>());
            final XMPPNotificationAccessor router = new XMPPNotificationAccessor(metadata,
                    this,
                    resourceName);
            resource.put(router);
            return router;
        } finally {
            writeLock.unlock();
        }
    }

    XMPPNotificationAccessor disableNotifications(final String resourceName,
                                              final MBeanNotificationInfo metadata){
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try{
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.get(resourceName);
            if(resource == null) return null;
            final XMPPNotificationAccessor accessor = resource.remove(metadata);
            if(resource.isEmpty())
                notifications.remove(resourceName);
            return accessor;
        } finally {
            writeLock.unlock();
        }
    }

    Iterable<XMPPNotificationAccessor> clear(final String resourceName){
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try{
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.remove(resourceName);
            return resource != null ? resource.values() : ImmutableList.of();
        } finally {
            writeLock.unlock();
        }
    }

    void clear(){
        removeAll();
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try{
            notifications.values().forEach(ResourceFeatureList::clear);
            notifications.clear();
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public <E extends Exception> void forEachNotification(final EntryReader<String, ? super XMPPNotificationAccessor, E> notificationReader) throws E {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try{
            for(final ResourceNotificationList<XMPPNotificationAccessor> list: notifications.values())
                for(final XMPPNotificationAccessor accessor: list.values())
                    if(!notificationReader.read(accessor.resourceName, accessor)) return;
        }finally {
            readLock.unlock();
        }
    }
}
