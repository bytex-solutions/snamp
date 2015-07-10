package com.itworks.snamp.adapters.groovy.dsl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itworks.snamp.Aggregator;
import com.itworks.snamp.internal.CallableSupplier;
import com.itworks.snamp.internal.annotations.SpecialUse;
import groovy.lang.GroovyObjectSupport;

import javax.management.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Provides access to connected resources from Groovy script.
 * @author Roman Sakno
 * @since 1.0
 */
public abstract class GroovyManagementModel extends GroovyObjectSupport implements Aggregator, AttributesView, EventsView {
    private final Cache<String, GroovyManagedResource> cache = CacheBuilder.newBuilder().maximumSize(15).build();

    /**
     * Returns a list of connected resources.
     *
     * @return A list of connected resources.
     */
    @SpecialUse
    public abstract Set<String> list();

    @SpecialUse
    @Override
    public abstract Set<String> getAttributes(final String resourceName);

    @SpecialUse
    @Override
    public abstract Collection<MBeanAttributeInfo> getAttributesMetadata(final String resourceName);

    @Override
    public abstract Collection<MBeanNotificationInfo> getEventsMetadata(final String resourceName);

    @SpecialUse
    @Override
    public abstract Set<String> getEvents(final String resourceName);

    @SpecialUse
    @Override
    public abstract Object getAttributeValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException;

    @SpecialUse
    @Override
    public abstract void setAttributeValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException;

    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return objectType != null && objectType.isInstance(this) ? objectType.cast(this) : null;
    }

    private GroovyManagedResource getOrPutResource(final String resourceName) {
        try {
            return cache.get(resourceName, new CallableSupplier<GroovyManagedResource>() {
                @Override
                public GroovyManagedResource get() {
                    return new GroovyManagedResource(GroovyManagementModel.this, resourceName);
                }
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public final Object getProperty(final String property) {
        if (list().contains(property))
            return getOrPutResource(property);
        else {
            cache.invalidate(property);
            return super.getProperty(property);
        }
    }
}
