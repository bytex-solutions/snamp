package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.google.common.collect.ImmutableSet;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class JmxNotificationAccessor extends NotificationAccessor implements JmxFeatureBindingInfo<MBeanNotificationInfo> {
    private final String resourceName;
    private final WeakReference<Consumer<Notification>> listenerRef;

    JmxNotificationAccessor(final String resourceName,
                            final MBeanNotificationInfo metadata,
                            final Consumer<Notification> destination) {
        super(metadata);
        this.resourceName = resourceName;
        listenerRef = new WeakReference<>(destination);
    }

    //cloning metadata is required because RMI class loader will raise ClassNotFoundException: NotificationDescriptor (no security manager: RMI class loader disabled)
    @Override
    public MBeanNotificationInfo cloneMetadata() {
        return new MBeanNotificationInfo(getMetadata().getNotifTypes(),
                Notification.class.getName(),   //to avoid serialization problems in JMX
                getMetadata().getDescription(),
                cloneDescriptor());
    }

    private ImmutableDescriptor cloneDescriptor(){
        return JmxFeatureBindingInfo.cloneDescriptor(getDescriptor());
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final Consumer<Notification> listener = listenerRef.get();
        if (listener != null) {
            //avoid serialization problems in JMX: just create clone of type Notification
            listener.accept(new NotificationBuilder(notification).setSource(resourceName).get());
        }
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

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    public void close() {
        listenerRef.clear();
        super.close();
    }
}
