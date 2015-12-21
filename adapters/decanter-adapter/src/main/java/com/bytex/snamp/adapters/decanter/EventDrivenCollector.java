package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.adapters.modeling.ModelOfNotifications;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.adapters.modeling.ResourceNotificationList;
import com.google.common.collect.ImmutableList;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents event-driven collector used to harvest resource events.
 * This class cannot be inherited.
 */
final class EventDrivenCollector extends ModelOfNotifications<DecanterNotificationAccessor> implements DecanterCollector, SafeCloseable {
    private final EventAdmin eventAdmin;
    private final ReadWriteLock resourcesLock;
    private final Map<String, ResourceNotificationList<DecanterNotificationAccessor>> resources;

    EventDrivenCollector(final EventAdmin admin){
        this.eventAdmin = Objects.requireNonNull(admin);
        resources = new HashMap<>(10);
        resourcesLock = new ReentrantReadWriteLock();
    }

    DecanterNotificationAccessor addNotification(final String resourceName, final MBeanNotificationInfo metadata) {
        final DecanterNotificationAccessor result;
        final Lock writeLock = resourcesLock.writeLock();
        writeLock.lock();
        try {
            final ResourceNotificationList<DecanterNotificationAccessor> list;
            if (resources.containsKey(resourceName))
                list = resources.get(resourceName);
            else resources.put(resourceName, list = new ResourceNotificationList<>());
            list.put(result = new DecanterNotificationAccessor(metadata, eventAdmin, TOPIC_PREFIX.concat(resourceName)));
        } finally {
            writeLock.unlock();
        }
        return result;
    }

    DecanterNotificationAccessor removeNotification(final String resourceName, final MBeanNotificationInfo metadata) {
        final DecanterNotificationAccessor result;
        final Lock writeLock = resourcesLock.writeLock();
        writeLock.lock();
        try {
            final ResourceNotificationList<DecanterNotificationAccessor> list;
            if (resources.containsKey(resourceName))
                list = resources.get(resourceName);
            else return null;
            result = list.remove(metadata);
            if (list.isEmpty()) resources.remove(resourceName);
        } finally {
            writeLock.unlock();
        }
        return result;
    }

    Collection<DecanterNotificationAccessor> clear(final String resourceName){
        final Lock writeLock = resourcesLock.writeLock();
        writeLock.lock();
        try {
            return resources.containsKey(resourceName) ?
                    resources.remove(resourceName).values():
                    ImmutableList.<DecanterNotificationAccessor>of();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close(){
        final Lock writeLock = resourcesLock.writeLock();
        writeLock.lock();
        try {
            for(final ResourceNotificationList<?> list: resources.values())
                list.clear();
            resources.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public synchronized <E extends Exception> void forEachNotification(final EntryReader<String, ? super DecanterNotificationAccessor, E> notificationReader) throws E {
        final Lock readLock = resourcesLock.readLock();
        readLock.lock();
        try {
            for (final String resourceName : resources.keySet())
                for (final DecanterNotificationAccessor accessor : resources.get(resourceName).values())
                    if (!notificationReader.read(resourceName, accessor)) return;
        } finally {
            readLock.unlock();
        }
    }
}