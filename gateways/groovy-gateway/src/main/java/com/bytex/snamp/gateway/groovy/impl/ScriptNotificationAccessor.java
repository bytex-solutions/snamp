package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanNotificationInfo;
import java.util.Set;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ScriptNotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    ScriptNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(resourceName, metadata, destination);
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
     * This operation may change behavior of the gateway.
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
