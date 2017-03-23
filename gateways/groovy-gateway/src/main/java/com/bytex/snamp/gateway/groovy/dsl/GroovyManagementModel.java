package com.bytex.snamp.gateway.groovy.dsl;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.SpecialUse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import groovy.lang.GroovyObjectSupport;

import javax.annotation.Nonnull;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Provides access to connected resources from Groovy script.
 * @author Roman Sakno
 * @since 1.0
 */
public abstract class GroovyManagementModel extends GroovyObjectSupport implements Aggregator, AttributesView, EventsView, ResourcesView {
    private final Cache<String, GroovyManagedResource> cache = CacheBuilder.newBuilder().maximumSize(15).build();

    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return objectType.isInstance(this) ? objectType.cast(this) : null;
    }

    private GroovyManagedResource getOrPutResource(final String resourceName) {
        return callUnchecked(() -> cache.get(resourceName, () -> new GroovyManagedResource(GroovyManagementModel.this, resourceName)));
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public final GroovyManagedResource getResource(final String resourceName) {
        if (getList().contains(resourceName))
            return getOrPutResource(resourceName);
        else {
            cache.invalidate(resourceName);
            return null;
        }
    }

    @Override
    public final Object getProperty(final String property) {
        final GroovyManagedResource result = getResource(property);
        return result == null ? super.getProperty(property) : result;
    }
}