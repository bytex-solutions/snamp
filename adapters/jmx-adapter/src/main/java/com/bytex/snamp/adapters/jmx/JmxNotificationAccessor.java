package com.bytex.snamp.adapters.jmx;

import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.ImmutableSet;
import com.bytex.snamp.adapters.modeling.NotificationAccessor;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.ref.WeakReference;
import java.util.Set;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JmxNotificationAccessor extends NotificationAccessor implements FeatureBindingInfo<MBeanNotificationInfo> {
    private final String resourceName;
    private final WeakReference<NotificationListener> listenerRef;

    JmxNotificationAccessor(final String resourceName,
                            final MBeanNotificationInfo metadata,
                            final NotificationListener destination) {
        super(metadata);
        this.resourceName = resourceName;
        listenerRef = new WeakReference<>(destination);
    }

    //cloning metadata is required because RMI class loader will raise ClassNotFoundException: NotificationDescriptor (no security manager: RMI class loader disabled)
    MBeanNotificationInfo cloneMetadata() {
        return new MBeanNotificationInfo(getMetadata().getNotifTypes(),
                getMetadata().getName(),
                getMetadata().getDescription(),
                DescriptorUtils.copyOf(getMetadata().getDescriptor()));
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(resourceName);
        final NotificationListener listener = listenerRef.get();
        if (listener != null) listener.handleNotification(notification, handback);
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
