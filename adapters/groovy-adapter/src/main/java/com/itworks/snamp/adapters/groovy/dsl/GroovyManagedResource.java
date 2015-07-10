package com.itworks.snamp.adapters.groovy.dsl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itworks.snamp.internal.CallableSupplier;
import com.itworks.snamp.internal.annotations.SpecialUse;
import groovy.lang.GroovyObjectSupport;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Represents wrapper for collection of attributes. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 */
public final class GroovyManagedResource extends GroovyObjectSupport {
    private final String resourceName;
    private final AttributesView attributes;
    private final EventsView events;
    private final Cache<String, GroovyResourceAttribute> attrCache = CacheBuilder.newBuilder().maximumSize(15).build();

    <T extends AttributesView & EventsView> GroovyManagedResource(final T model,
                                                                  final String resourceName) {
        this.resourceName = resourceName;
        this.attributes = model;
        this.events = model;
    }

    /**
     * Returns a collection of all attributes in this resource.
     *
     * @return A collection of all attributes in this resource.
     */
    @SpecialUse
    public final Set<String> getAttributes() {
        return attributes.getAttributes(resourceName);
    }

    @SpecialUse
    public final Set<String> getEvents(){
        return events.getEvents(resourceName);
    }

    private static GroovyResourceAttribute getOrPutAttribute(final Cache<String, GroovyResourceAttribute> cache,
                                                             final String resourceName,
                                                             final String attributeName,
                                                             final AttributesView attributes) {
        try {
            return cache.get(attributeName, new CallableSupplier<GroovyResourceAttribute>() {
                @Override
                public GroovyResourceAttribute get() {
                    return new GroovyResourceAttribute(attributes, resourceName, attributeName);
                }
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private static MBeanNotificationInfo findEvent(final String eventName,
                                                   final Collection<MBeanNotificationInfo> events){
        for(final MBeanNotificationInfo metadata: events)
            for(final String notifType: metadata.getNotifTypes())
                if(Objects.equals(notifType, eventName))
                    return metadata;
        return null;
    }

    @Override
    public Object getProperty(final String entityName) {
        if (getAttributes().contains(entityName))
            return getOrPutAttribute(attrCache, resourceName, entityName, attributes);
        else if (getEvents().contains(entityName))
            return findEvent(entityName, events.getEventsMetadata(resourceName));
        else {
            synchronized (this) {
                attrCache.invalidate(entityName);
            }
            return super.getProperty(entityName);
        }
    }

    @Override
    public String toString() {
        return resourceName;
    }
}
