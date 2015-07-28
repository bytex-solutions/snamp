package com.bytex.snamp.adapters.groovy.dsl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.bytex.snamp.internal.CallableSupplier;
import com.bytex.snamp.internal.annotations.SpecialUse;
import groovy.lang.GroovyObjectSupport;

import javax.management.MBeanNotificationInfo;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Represents wrapper for collection of attributes. This class cannot be inherited or instantiated
 * directly from your code.
 * @author Roman Sakno
 * @since 1.0
 */
public final class GroovyManagedResource extends GroovyObjectSupport {
    private final String resourceName;
    private final AttributesView attributes;
    private final EventsView events;
    private final ResourcesView resources;
    private final Cache<String, GroovyResourceAttribute> attrCache = CacheBuilder.newBuilder().maximumSize(15).build();
    private final Cache<String, GroovyResourceEvent> eventCache = CacheBuilder.newBuilder().maximumSize(10).build();

    <T extends AttributesView & EventsView & ResourcesView> GroovyManagedResource(final T model,
                                                                  final String resourceName) {
        this.resourceName = resourceName;
        this.attributes = model;
        this.events = model;
        this.resources = model;
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
    public final Set<String> getEvents() {
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
        } catch (final ExecutionException ignored) {
            return null;
        }
    }

    private static GroovyResourceEvent getOrPutEvent(final Cache<String, GroovyResourceEvent> cache,
                                                     final String resourceName,
                                                     final String eventName,
                                                     final EventsView events) {
        try {
            return cache.get(eventName, new Callable<GroovyResourceEvent>() {
                @Override
                public GroovyResourceEvent call() throws InvalidKeyException {
                    for (final MBeanNotificationInfo metadata : events.getEventsMetadata(resourceName))
                        for (final String notifType : metadata.getNotifTypes())
                            if (Objects.equals(notifType, eventName))
                                return new GroovyResourceEvent(metadata);
                    throw new InvalidKeyException();
                }
            });
        } catch (final ExecutionException ignored) {
            return null;
        }
    }

    @SpecialUse
    public GroovyResourceAttribute getAttribute(final String attributeName) {
        if (getAttributes().contains(attributeName))
            return getOrPutAttribute(attrCache, resourceName, attributeName, attributes);
        else {
            attrCache.invalidate(attributeName);
            return null;
        }
    }

    @SpecialUse
    public GroovyResourceEvent getEvent(final String eventName) {
        if (getEvents().contains(eventName))
            return getOrPutEvent(eventCache, resourceName, eventName, events);
        else {
            eventCache.invalidate(eventName);
            return null;
        }
    }

    /**
     * Gets metadata of this resource.
     * @return The metadata of this resource.
     */
    public Map<String, ?> getMetadata(){
        return resources.getResourceParameters(resourceName);
    }

    @Override
    public Object getProperty(final String entityName) {
        final GroovyResourceAttribute attribute = getAttribute(entityName);
        if (attribute == null) {
            final GroovyResourceEvent event = getEvent(entityName);
            return event == null ? super.getProperty(entityName) : event;
        } else return attribute;
    }

    @Override
    public String toString() {
        return resourceName;
    }
}
