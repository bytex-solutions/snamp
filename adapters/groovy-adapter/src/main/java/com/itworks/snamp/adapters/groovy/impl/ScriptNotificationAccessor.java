package com.itworks.snamp.adapters.groovy.impl;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.modeling.NotificationRouter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Set;

import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptNotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    private final String resourceName;

    ScriptNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(metadata, destination);
        this.resourceName = resourceName;
    }

    public String getResourceName(){
        return resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    /**
     * Gets binding property such as URL, OID or any other information
     * describing how this feature is exposed to the outside world.
     *
     * @param propertyName The name of the binding property.
     * @return The value of the binding property.
     */
    @Override
    public Object getProperty(final String propertyName) {
        return null;
    }

    /**
     * Gets all supported properties.
     *
     * @return A set of all supported properties.
     */
    @Override
    public Set<String> getProperties() {
        return ImmutableSet.of();
    }

    /**
     * Overwrite property value.
     * <p/>
     * This operation may change behavior of the resource adapter.
     *
     * @param propertyName The name of the property to change.
     * @param value        A new property value.
     * @return {@literal true}, if the property supports modification and changed successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }
}
