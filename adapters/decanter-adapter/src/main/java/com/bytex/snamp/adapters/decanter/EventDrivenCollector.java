package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.adapters.modeling.ModelOfNotifications;
import com.bytex.snamp.adapters.modeling.ResourceFeatureList;
import com.bytex.snamp.adapters.modeling.ResourceNotificationList;
import com.google.common.collect.ImmutableList;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents event-driven collector used to harvest resource events.
 * This class cannot be inherited.
 */
final class EventDrivenCollector extends ModelOfNotifications<DecanterNotificationAccessor> implements DecanterCollector, SafeCloseable {
    private enum ResourceGroups{
        RESOURCES
    }
    private final EventAdmin eventAdmin;
    private final Map<String, ResourceNotificationList<DecanterNotificationAccessor>> resources;

    EventDrivenCollector(final EventAdmin admin){
        super(ResourceGroups.class);
        this.eventAdmin = Objects.requireNonNull(admin);
        resources = new HashMap<>(10);
    }

    private DecanterNotificationAccessor addNotificationImpl(final String resourceName, final MBeanNotificationInfo metadata){
        final DecanterNotificationAccessor result;
        final ResourceNotificationList<DecanterNotificationAccessor> list;
        if (resources.containsKey(resourceName))
            list = resources.get(resourceName);
        else resources.put(resourceName, list = new ResourceNotificationList<>());
        list.put(result = new DecanterNotificationAccessor(metadata, eventAdmin, TOPIC_PREFIX.concat(resourceName)));
        return result;
    }

    DecanterNotificationAccessor addNotification(final String resourceName, final MBeanNotificationInfo metadata) {
        return writeApply(ResourceGroups.RESOURCES, resourceName, metadata, this::addNotificationImpl);
    }

    private DecanterNotificationAccessor removeNotificationImpl(final String resourceName, final MBeanNotificationInfo metadata){
        final DecanterNotificationAccessor result;
        final ResourceNotificationList<DecanterNotificationAccessor> list;
        if (resources.containsKey(resourceName))
            list = resources.get(resourceName);
        else return null;
        result = list.remove(metadata);
        if (list.isEmpty()) resources.remove(resourceName);
        return result;
    }

    DecanterNotificationAccessor removeNotification(final String resourceName, final MBeanNotificationInfo metadata) {
        return writeApply(ResourceGroups.RESOURCES, resourceName, metadata, this::removeNotificationImpl);
    }

    Collection<DecanterNotificationAccessor> clear(final String resourceName) {
        return writeApply(ResourceGroups.RESOURCES, resourceName, resources, (resName, res) -> res.containsKey(resName) ?
                res.remove(resName).values() :
                ImmutableList.of());
    }

    @Override
    public void close(){
        writeAccept(ResourceGroups.RESOURCES, resources, res -> {
            res.values().forEach(ResourceFeatureList::clear);
            res.clear();
        });
    }

    private <E extends Exception> void forEachNotificationImpl(final EntryReader<String, ? super DecanterNotificationAccessor, E> notificationReader) throws E{
        for (final String resourceName : resources.keySet())
            for (final DecanterNotificationAccessor accessor : resources.get(resourceName).values())
                if (!notificationReader.read(resourceName, accessor)) return;
    }

    @Override
    public <E extends Exception> void forEachNotification(final EntryReader<String, ? super DecanterNotificationAccessor, E> notificationReader) throws E {
        readAccept(ResourceGroups.RESOURCES, notificationReader, this::forEachNotificationImpl);
    }
}
